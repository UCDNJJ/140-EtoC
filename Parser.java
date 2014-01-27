/* *** This file is given as part of the programming assignment. *** */
//Caroline Chan, Tiffany Chan, Julien Hoachuck, Nelson Johansen
import java.util.*;

public class Parser {

    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
        tok = scanner.scan();
    }

    private Scan scanner;
    Parser(Scan scanner) {
        this.scanner = scanner;
        scan();
        program();
        if( tok.kind != TK.EOF )
            parse_error("junk after logical end of program");
    }
    //Symbol Table data structure
    HashMap<String,Stack<Vars>> symTable = new HashMap<String,Stack<Vars>>();
    //Holds data about which variables were declared at each depth.
    HashMap<Integer,HashMap<String,Token>> currentDepth = new HashMap<Integer,HashMap<String,Token>>();
    
    //depth variable
    int depth;
  
    Token currVar;
    
    private void program() {
        block();
    }

    private void block() {       
        if(is(TK.VAR))
        {
            declarations();
        }
        else
        {
            //To avoid nullPointerException if depth >= 1 and no variables were declared on depth x
            currentDepth.put(depth, null);
        }


        statement_list();
        removeSymbols();
    }
    
    private void updateSymbols() {
        //create a new class for the current declaration of the variable
        Vars var = new Vars();
        //stack to hold variables
        Stack<Vars> stack = new Stack<Vars>();
        
        if(!(symTable.containsKey(tok.string)))
        {                
            var.line_declared = tok.lineNumber;
            var.nesting_depth = depth; 
            
            stack.push(var);
            symTable.put(tok.string, stack);               
        }
        else //symTable contains the key and its stack already!!!
        {
            stack = symTable.get(tok.string);
                
            var.line_declared = tok.lineNumber;
            var.nesting_depth = depth;
                
            stack.push(var);
            symTable.put(tok.string, stack);
        }
    }
    
    private void removeSymbols() {
        //stack to hold variables
        Stack<Vars> stack = new Stack<Vars>();
        //holds the key values to be poped for current depth
        HashMap<String,Token> temp = new HashMap<String,Token>();
        
        temp = currentDepth.get(depth);
        
        if(temp == null)
            return;
        
        for(String key: temp.keySet())
        {
            stack = symTable.get(key);
            stack.pop();
            
            if(stack.isEmpty())
                symTable.remove(key);
            else
                symTable.put(key, stack);
        }        
    }
    
    private void variableAssigned() {
        //create a new class for the current declaration of the variable
        Vars var = new Vars();
        //stack to hold variables
        Stack<Vars> stack = new Stack<Vars>();

        if(symTable.containsKey(tok.string))
        {
            stack = symTable.get(tok.string);
            var = stack.pop();
            
            var.assigned.add(tok.lineNumber);
            
            stack.push(var);
            symTable.put(tok.string, stack);
        }
        else
        {
            System.out.println("undeclared variable " + tok.string +" on line " + tok.lineNumber);
            System.exit(1);
        }
    }
    
    private void variableUsed(Token t) {
        //create a new class for the current declaration of the variable
        Vars var = new Vars();
        //stack to hold variables
        Stack<Vars> stack = new Stack<Vars>();
        
        if(symTable.containsKey(t.string))
        {
            stack = symTable.get(t.string);
            var = stack.pop();
            
            var.used.add(t.lineNumber);
            
            stack.push(var);
            symTable.put(t.string, stack);
        }
        else
        {
            System.out.println("undeclared variable " + t.string +" on line " + t.lineNumber);
            System.exit(1);
        }
        
    }

    private void declarations() {
        mustbe(TK.VAR);
        
        //redeclartion checking structure and current block variables to pop before leaving scope
        HashMap<String,Token> sentinel = new HashMap<String,Token>();
        
        while( is(TK.ID) ) {
            if(!(sentinel.containsKey(tok.string)))
            {
                sentinel.put(tok.string, tok);
                updateSymbols();
            }
            else
            {
                System.out.println("variable " + tok.string + " is redeclared on line " + tok.lineNumber);
                updateSymbols();//even though symbol is redeclared it is still used
                                //within current scope.
            }
            
            scan();
            
        }
        currentDepth.put(depth, sentinel);
        mustbe(TK.RAV);
    }
    
    private void statement_list() {
        
        while(is(TK.ID) || is(TK.PRINT) || is(TK.IF) || is(TK.DO) || is(TK.FA))
        {
            statement();
        }
        
    }

    private void statement() {
        
        if(is(TK.ID))
        {
            variableAssigned();
            assign();
        }
        else if(is(TK.PRINT))
        {
            print();
        }
        else if(is(TK.IF))
        {
            depth++;
            x_if();
            depth--;
        }
        else if(is(TK.DO))
        {
            depth++;
            x_do();
            depth--;
        }
        else
        {
            depth++;
            x_fa();
            depth--;
        }
    }
    
    private void assign() {
        
        mustbe(TK.ID);
        mustbe(TK.ASSIGN);
        expr();
    }
    
    private void print() {
        mustbe(TK.PRINT);
        expr();
    }
    
    private void x_if() {
        mustbe(TK.IF);
        guarded_commands();
        mustbe(TK.FI);
    }
    
    private void x_do() {
        mustbe(TK.DO);
        guarded_commands();
        mustbe(TK.OD);
    }
    
    private void x_fa() {
        mustbe(TK.FA);
        variableAssigned();
        mustbe(TK.ID);
        mustbe(TK.ASSIGN);
        
        expr();
        
        mustbe(TK.TO);
        
        expr();

        if(is(TK.ST))
        {
            mustbe(TK.ST);
            expr();
        }
        
        commands();

        mustbe(TK.AF);
    }
    
    private void expr() {
        simple();
        
        if(is(TK.EQ) || is(TK.LT) || is(TK.GT) || is(TK.NE) 
                || is(TK.LE) || is(TK.GE)) 
        {
            if(is(TK.ID))
                variableUsed(currVar);
            
            scan();
            
            if(is(TK.ID))
                variableUsed(tok);
            
            simple();
        }
    }
    
    private void simple() {
        term();

        while(is(TK.PLUS) || is(TK.MINUS))
        {
            if(is(TK.ID))
                variableUsed(currVar);
            
            scan();
            
            if(is(TK.ID))
                variableUsed(tok);
            
            term();
        }
    }
    
    private void term() {
        factor();

        while(is(TK.TIMES) || is(TK.DIVIDE))
        {
            if(is(TK.ID))
                variableUsed(currVar);
            
            scan();
            
            if(is(TK.ID))
                variableUsed(tok);
            
            factor();
        }
    }
    
    private void factor() {
        
        if(is(TK.LPAREN))
        {
            mustbe(TK.LPAREN);
            expr();
            mustbe(TK.RPAREN);
        }
        else if(is(TK.ID))
        {
            currVar = tok;
            if(!(symTable.containsKey(tok.string)))
                System.out.println("undeclared variable " + tok.string +" on line " + tok.lineNumber);
            
            mustbe(TK.ID);
        }
        else if(is(TK.NUM))
        {
            mustbe(TK.NUM);
        }
        else
        {
            parse_error("factor");
        }
    }
    
    private void guarded_commands() {
        guarded_command();
        
        while(is(TK.BOX))
        {
            mustbe(TK.BOX);
            guarded_command();
        }
        
        if(is(TK.ELSE))
        {
            mustbe(TK.ELSE);
            commands();
        }             
        
    }
    
    private void guarded_command() {
        expr();
        commands();
    }
    
    private void commands() {
        mustbe(TK.ARROW);
        block();
    }

    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
        if( ! is(tk) ) {
            System.err.println( "mustbe: want " + tk + ", got " +
                                    tok);
            parse_error( "missing token (mustbe)" );
        }
        scan();
    }

    private void parse_error(String msg) {
        System.err.println( "can't parse: line "
                            + tok.lineNumber + " " + msg );
        System.exit(1);
    }
}