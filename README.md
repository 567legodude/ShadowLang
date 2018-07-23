# ShadowLang
This is a parsed scripting language I made as a challenge to myself to see if I could do it.

Every aspect of the language can be represented below:

    block modifier (parameter) {
        keyword argument replacer{?}
    }

Blocks are signified by a line ending with a bracket `{`  
The first word of the line is the block name, any others are modifiers.
Blocks can have parameters which are enclosed in parentheses after the modifiers.
Modifiers should be used to give the block information, and parameters are variables that will be filled and given to the scope of the block.  
Blocks are defined with: Number of expected modifiers, number of expected parameters, which section parser to use for modifiers, the entry condition to enter the block, what happens when the scope enters the block, what happens when the end of the block is reached.

Keywords are similar to blocks but do not end with a bracket.  
The first word of the line is the keyword name, others are arguments.  
Keywords are defined with: A splitter to split the arguments during parsing, section parser to use for arguments, number of expected sections, how to execute the keyword.

Replacers have a keyword that is followed by brackets with some data inside `replacer{?}`. Replacers can be used as a way to preprocess some data before it is used as a modifier or argument.

The language has a built in evaluator which reads evaluator strings from left to right and executes the symbols.
Currently it is used mostly for parsing Java reflection to give it nearly the full functionality a program could need.
