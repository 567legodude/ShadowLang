# ShadowLang
This is a side project that isn't developed very often.

Shadow is a parsed scripting language I made as a challenge to myself to see if I could do it.

Every aspect of the language can be represented below:

    block argument -> parameter {
        keyword argument [inline_keyword]
    }
    
    one_line_block :: keyword

Info
----

The language is made up of commands, which have return values and may be used as arguments to other commands, however the language is backed by Java.  

The script files are able to be parsed and executed directly, but can also be compiled to equivalent Java code.

There are two entities in the language: blocks, and keywords. The difference is that blocks have a body and local scope parameters.

##### Blocks
Blocks are lines of code that end with a bracket `{` or contain the block operator `::`.
Sections that follow the block name are the block arguments, however on blocks these may be called modifiers.
Blocks can have local scope parameters which are separated from the modifiers using the operator `->` and delimited with commas.

Modifiers should be used to give the block state information that affects if/how it runs, and parameters are variables that will be set and can be used within the block.  

Blocks are used to control the flow of a program or to affect the state of the entities within them, but do not execute actions on their own.

Example:
```
# Assume names is an ArrayList object
foreach names -> name {
    print name
}
```

##### Keywords
Keywords perform the actions of the program.  
The first token on the line is the action to perform and all following sections are the arguments to that command.

Square brackets are used for inline keywords, which is the way the return value of a keyword is used as an argument to another keyword.  
Example: `print [type "some string"]`  
On its own, the `type` keyword has no function, but the square brackets make it an inline keyword and pass its return value to print, which is `"String"` in this case.
