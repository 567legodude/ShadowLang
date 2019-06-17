# ShadowLang
This is a parsed scripting language I made as a challenge to myself to see if I could do it.

Every aspect of the language can be represented below:

    block argument -> parameter {
        keyword argument [inline_keyword]
    }
    
    one_line_block :: keyword

Info
----

All entities in the language have a command style, where the first word on the line is the action and everything that follows is input data.  

There are two entities in the language: blocks, and keywords, which contain the sections of input data that allow the code to run.

##### Blocks
Blocks are lines of code that end with a bracket `{` or contain the block operator `::`.
Sections that follow the block name are the block arguments, however on blocks these may be called modifiers.
Blocks can have parameters which are separated from the modifiers using the operator `->` and delimited with commas.

Modifiers should be used to give the block state information that affects if/how it runs, and parameters are variables that will be set and can be used within the block.  

Blocks are used to control the flow of a program or to affect the state of the entities within them, but do not execute actions on their own.

##### Keywords
Keywords perform the actions of the program.  
The first token on the line is the action to perform and all following sections are the arguments to that command.

Square brackets are used to access the return value of keywords as input to other entities.  
Example: `print [type "some string"]`  
On its own, the `type` keyword has no function, but the square brackets allow using it within another keyword to get the return value, which is `"String"` in this case.
