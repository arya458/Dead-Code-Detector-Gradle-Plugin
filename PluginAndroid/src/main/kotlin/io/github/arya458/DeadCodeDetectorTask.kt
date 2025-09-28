package io.github.arya458

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.FieldVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import java.io.FileInputStream

// --- Data classes for storing code structure information ---
data class MethodInfo(
    val name: String,
    val descriptor: String,
    val access: Int,
    val methodCalls: MutableSet<String> = mutableSetOf(), // Stores methodId of called methods
    val fieldAccesses: MutableSet<String> = mutableSetOf() // Stores fieldId of accessed fields
)

data class FieldInfo(
    val name: String,
    val descriptor: String,
    val access: Int
)

data class ClassInfo(
    val name: String, // Fully qualified name with '.' as separator
    val access: Int,
    val superName: String?,
    val interfaces: List<String>,
    val methods: MutableList<MethodInfo> = mutableListOf(),
    val fields: MutableList<FieldInfo> = mutableListOf()
)

abstract class DeadCodeDetectorTask : DefaultTask() {

    @get:Input
    abstract val failOnDeadCode: Property<Boolean>

    @get:Input
    abstract val includeTests: Property<Boolean>

    @get:Input
    abstract val keepPublicApi: Property<Boolean>

    @get:Input
    abstract val includeResources: Property<Boolean>

    @get:Input
    abstract val excludePackages: ListProperty<String>

    @get:Input
    abstract val keepAnnotations: ListProperty<String>

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val allClasses: ConfigurableFileCollection

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val allResourceDirs: ConfigurableFileCollection

    @get:OutputFile
    abstract val reportFile: RegularFileProperty

    @get:Internal
    protected val classInfos = mutableMapOf<String, ClassInfo>()

    @get:Internal
    protected val liveElements = mutableSetOf<String>()

    private fun classId(className: String) = className
    private fun methodId(className: String, methodName: String, methodDesc: String) = "${className}.${methodName}${methodDesc}"
    private fun fieldId(className: String, fieldName: String, fieldDesc: String) = "${className}.${fieldName}:${fieldDesc}"

    @TaskAction
    fun detectDeadCode() {
        logger.lifecycle("Running Dead Code Detector Task...")
        classInfos.clear()
        liveElements.clear()

        logger.lifecycle("Configuration: ...") // Simplified for brevity

        logger.lifecycle("Scanning class files and method bodies...")
        allClasses.asFileTree.filter { it.isFile && it.name.endsWith(".class") }.forEach { classFile ->
            try {
                FileInputStream(classFile).use { fis ->
                    val classReader = ClassReader(fis)
                    // Pass 0 to read method bodies
                    classReader.accept(DeadCodeClassVisitor(this), 0) 
                }
            } catch (e: Exception) {
                logger.warn("Could not parse class file: ${classFile.absolutePath}", e)
            }
        }
        logger.lifecycle("Collected information for ${classInfos.size} classes.")

        identifyInitialLiveElements()
        logger.lifecycle("Identified ${liveElements.size} initial live elements.")

        performReachabilityAnalysis()
        logger.lifecycle("Reachability analysis complete. Total live elements: ${liveElements.size}")

        val reportContent = generateReport()
        reportFile.get().asFile.writeText(reportContent)
        logger.lifecycle("Report generated at: ${reportFile.get().asFile.absolutePath}")

        if (failOnDeadCode.get() && hasDeadCode()) {
            throw IllegalStateException("Dead code detected. See report for details.")
        }
    }

    private fun identifyInitialLiveElements() {
        val shouldKeepPublicApi = keepPublicApi.getOrElse(false)
        classInfos.values.forEach { classInfo ->
            if (shouldKeepPublicApi && (classInfo.access and Opcodes.ACC_PUBLIC != 0)) {
                liveElements.add(classId(classInfo.name))
            }
            classInfo.methods.forEach { methodInfo ->
                val currentMethodId = methodId(classInfo.name, methodInfo.name, methodInfo.descriptor)
                if (methodInfo.name == "<init>" || methodInfo.name == "<clinit>") {
                    liveElements.add(currentMethodId)
                } else if (shouldKeepPublicApi && (methodInfo.access and Opcodes.ACC_PUBLIC != 0)) {
                    liveElements.add(currentMethodId)
                }
            }
            classInfo.fields.forEach { fieldInfo ->
                if (shouldKeepPublicApi && (fieldInfo.access and Opcodes.ACC_PUBLIC != 0)) {
                    liveElements.add(fieldId(classInfo.name, fieldInfo.name, fieldInfo.descriptor))
                }
            }
        }
    }

    private fun performReachabilityAnalysis() {
        var lastSize: Int
        do {
            lastSize = liveElements.size
            val elementsToAdd = mutableSetOf<String>()

            liveElements.toList().forEach { liveElementId -> // Iterate over a copy to avoid concurrent modification
                // Is it a method? Format: ClassName.methodName(descriptor)V
                // A more robust way to parse this ID might be needed if class/method names contain '.' or '('
                val parts = liveElementId.split('.', '(')
                if (parts.size >= 2 && liveElementId.contains('(') && liveElementId.contains(')')) { // Likely a method ID
                    val className = parts[0]
                    // Method name and descriptor needs careful reconstruction if method name itself has '.'
                    // For simplicity, assuming method name is parts[1] and descriptor is reconstructed
                    val methodOwnerClass = classInfos[className]
                    methodOwnerClass?.methods?.find { methodId(className, it.name, it.descriptor) == liveElementId }?.let { liveMethod ->
                        elementsToAdd.addAll(liveMethod.methodCalls)
                        elementsToAdd.addAll(liveMethod.fieldAccesses)
                    }
                }
            }
            liveElements.addAll(elementsToAdd)
        } while (liveElements.size > lastSize)
    }

    private fun generateReport(): String {
        // ... (report generation logic - remains largely the same but uses the updated liveElements)
        val sb = StringBuilder()
        val lineSeparator = System.lineSeparator()

        val deadMethodsList = mutableListOf<Pair<String, MethodInfo>>()
        val deadFieldsList = mutableListOf<Pair<String, FieldInfo>>()
        val deadClassesList = mutableListOf<ClassInfo>()

        classInfos.values.forEach { classInfo ->
            val classIsConsideredLive = liveElements.contains(classId(classInfo.name))
            var classHasAnyLiveMember = false

            classInfo.methods.forEach { methodInfo ->
                val currentMethodId = methodId(classInfo.name, methodInfo.name, methodInfo.descriptor)
                if (liveElements.contains(currentMethodId)) {
                    classHasAnyLiveMember = true
                } else {
                    deadMethodsList.add(classInfo.name to methodInfo)
                }
            }
            classInfo.fields.forEach { fieldInfo ->
                val currentFieldId = fieldId(classInfo.name, fieldInfo.name, fieldInfo.descriptor)
                if (liveElements.contains(currentFieldId)) {
                    classHasAnyLiveMember = true
                } else {
                    deadFieldsList.add(classInfo.name to fieldInfo)
                }
            }
            
            // Class is dead if it's not explicitly live AND has no live members.
            // Constructors are members; if a class is instantiated, its <init> is live.
            if (!classIsConsideredLive && !classHasAnyLiveMember && classInfo.name != "module-info") {
                deadClassesList.add(classInfo)
            }
        }

        val deadMethodsCount = deadMethodsList.size
        val deadFieldsCount = deadFieldsList.size
        val deadClassesCount = deadClassesList.size
        val unusedDepsCount = 0 // Placeholder

        sb.append("╔══════════════════════════════════════════════════════════════════════════════════════════╗").append(lineSeparator)
        sb.append("║                            Dead Code Detector Report                                     ║").append(lineSeparator)
        sb.append("╚══════════════════════════════════════════════════════════════════════════════════════════╝").append(lineSeparator)
        sb.append(lineSeparator)
        sb.append("👤 Developed by Aria Danesh").append(lineSeparator)
        sb.append("📦 GitHub : https://github.com/arya458/Dead-Code-Detector-Gradle-Plugin").append(lineSeparator)
        sb.append("✉️  Email  : aria.danesh.work@gmail.com").append(lineSeparator)
        sb.append(lineSeparator)
        sb.append("─────────────────────────────── Summary ───────────────────────────────").append(lineSeparator)
        sb.append(String.format(" • Dead methods    : %d", deadMethodsCount)).append(lineSeparator)
        sb.append(String.format(" • Dead fields     : %d", deadFieldsCount)).append(lineSeparator)
        sb.append(String.format(" • Dead classes    : %d", deadClassesCount)).append(lineSeparator)
        sb.append(String.format(" • Unused deps     : %d (Not Implemented)", unusedDepsCount)).append(lineSeparator)
        sb.append("────────────────────────────────────────────────────────────────────────").append(lineSeparator)
        sb.append(lineSeparator)

        if (deadMethodsList.isNotEmpty()) {
            sb.append("🔧 Dead Methods:").append(lineSeparator)
            deadMethodsList.groupBy { it.first }.forEach { (className, methods) ->
                sb.append("   Class: $className").append(lineSeparator)
                methods.forEach { (_, methodInfo) ->
                    sb.append("     • ${methodInfo.name}${methodInfo.descriptor}").append(lineSeparator)
                }
            }
            sb.append(lineSeparator)
        }
        
        if (deadFieldsList.isNotEmpty()) {
            sb.append("🔧 Dead Fields:").append(lineSeparator)
            deadFieldsList.groupBy { it.first }.forEach { (className, fields) ->
                sb.append("   Class: $className").append(lineSeparator)
                fields.forEach { (_, fieldInfo) ->
                    sb.append("     • ${fieldInfo.name} (${fieldInfo.descriptor})").append(lineSeparator)
                }
            }
            sb.append(lineSeparator)
        }

        if (deadClassesList.isNotEmpty()) {
            sb.append("🗑️ Dead Classes:").append(lineSeparator)
            deadClassesList.forEach { classInfo ->
                sb.append("   • ${classInfo.name}").append(lineSeparator)
            }
            sb.append(lineSeparator)
        }

        sb.append("📦 Unused Dependencies: (Not Implemented)").append(lineSeparator)
        return sb.toString()
    }

    private fun hasDeadCode(): Boolean {
        // Check after full reachability analysis
        return classInfos.any { classInfo ->
            !liveElements.contains(classId(classInfo.value.name)) && classInfo.value.name != "module-info" || // Class itself might be dead
            classInfo.value.methods.any { !liveElements.contains(methodId(classInfo.value.name, it.name, it.descriptor)) } ||
            classInfo.value.fields.any { !liveElements.contains(fieldId(classInfo.value.name, it.name, it.descriptor)) }
        }
    }

    private class DeadCodeClassVisitor(private val task: DeadCodeDetectorTask) : ClassVisitor(Opcodes.ASM9) {
        private lateinit var currentClassNameInternal: String
        private lateinit var currentClassInfo: ClassInfo

        override fun visit(version: Int, access: Int, name: String, signature: String?, superName: String?, interfaces: Array<out String>?) {
            currentClassNameInternal = name.replace('/', '.')
            val actualSuperName = superName?.replace('/', '.')
            val actualInterfaces = interfaces?.map { it.replace('/', '.') } ?: emptyList()
            currentClassInfo = ClassInfo(
                name = currentClassNameInternal,
                access = access,
                superName = actualSuperName,
                interfaces = actualInterfaces
            )
            task.classInfos[currentClassNameInternal] = currentClassInfo
            task.logger.debug("Visiting class: $currentClassNameInternal")
            super.visit(version, access, name, signature, superName, interfaces)
        }

        override fun visitField(access: Int, name: String, descriptor: String, signature: String?, value: Any?): FieldVisitor? {
            if (::currentClassInfo.isInitialized) {
                val fieldInfo = FieldInfo(name = name, descriptor = descriptor, access = access)
                currentClassInfo.fields.add(fieldInfo)
            }
            return null
        }

        override fun visitMethod(access: Int, name: String, descriptor: String, signature: String?, exceptions: Array<out String>?): MethodVisitor? {
            if (::currentClassInfo.isInitialized) {
                val methodInfo = MethodInfo(name = name, descriptor = descriptor, access = access)
                currentClassInfo.methods.add(methodInfo)
                // Pass the task and the MethodInfo object to allow the visitor to store references
                return task.DeadCodeMethodVisitor(task, currentClassNameInternal, methodInfo)
            }
            return null
        }
    }

    // Inner class for visiting method bodies
    private inner class DeadCodeMethodVisitor(
        private val task: DeadCodeDetectorTask, // Outer class instance
        private val ownerClassName: String,
        private val ownerMethodInfo: MethodInfo // The method whose body is being visited
    ) : MethodVisitor(Opcodes.ASM9) {

        override fun visitMethodInsn(opcode: Int, owner: String, name: String, descriptor: String, isInterface: Boolean) {
            val targetOwnerClass = owner.replace('/', '.')
            val calledMethodId = task.methodId(targetOwnerClass, name, descriptor)
            ownerMethodInfo.methodCalls.add(calledMethodId)
            task.logger.debug("    [CALL] In ${task.methodId(ownerClassName, ownerMethodInfo.name, ownerMethodInfo.descriptor)} -> calls $calledMethodId")
            super.visitMethodInsn(opcode, owner, name, descriptor, isInterface)
        }

        override fun visitFieldInsn(opcode: Int, owner: String, name: String, descriptor: String) {
            val targetOwnerClass = owner.replace('/', '.')
            val accessedFieldId = task.fieldId(targetOwnerClass, name, descriptor)
            ownerMethodInfo.fieldAccesses.add(accessedFieldId)
            task.logger.debug("    [FIELD_ACCESS] In ${task.methodId(ownerClassName, ownerMethodInfo.name, ownerMethodInfo.descriptor)} -> accesses $accessedFieldId")
            super.visitFieldInsn(opcode, owner, name, descriptor)
        }
    }
}
