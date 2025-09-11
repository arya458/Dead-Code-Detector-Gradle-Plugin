package com.aria.danesh

//todo: @daedcodeignore
val foo1 = "Hello"

fun main() {
    val name = "Kotlin"
    val foo2 = "Hello"
    liveCode(name)
}

fun deadCode(name :String){
    println("DeadCode")
    println("Hello, " + name + "!")

    //TIP click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
    // To <b>Run</b> code, press <shortcut actionId="Run"/> or
    for (i in 1..5) {
        //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
        // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
        println("i = $i")
    }
}

fun liveCode(name :String){
    println("liveCode")
    println("Hello, " + name + "!")

    //TIP click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
    // To <b>Run</b> code, press <shortcut actionId="Run"/> or
    for (i in 1..5) {
        //TIP Press <shortcut actionId="Debug"/> to start debugging your code. We have set one <icon src="AllIcons.Debugger.Db_set_breakpoint"/> breakpoint
        // for you, but you can always add more by pressing <shortcut actionId="ToggleLineBreakpoint"/>.
        println("i = $i")
    }
}