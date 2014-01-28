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
    HashMap<Integer,ArrayList<String>> currentDepth = new HashMap<Integer,ArrayList<String>>();
    //Data structure to preserve values for printing
    Queue<Vars> symTablePreserved = new LinkedList<Vars>();
    //Global variable to allow printing iff program was valid
    boolean invalid;
    
    //depth variable
    int depth;
  
    Token currVar;
    
    private void program() {
        block();
        
        if(invalid == false)
            printPreserved();
    }

    private void block() {       
        if(is(TK.VAR))
        {
            declarations();
        }
        invalid = true;
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
            var.ID = tok.string;
            var.line_declared = tok.lineNumber;
            var.nesting_depth = depth; 
            
            stack.push(var);
            symTable.put(tok.string, stack);               
        }
        else //symTable contains the key and its stack already!!!
        {
            stack = symTable.get(tok.string);
            
            var.ID = tok.string;   
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
        ArrayList<String> temp = new ArrayList<String>();
        
        Vars var = new Vars();
        
        temp = currentDepth.get(depth);
        
        if(temp == null)
            return; 
        
        for(String key: temp)
        {
            stack = symTable.get(key);
            
            var = stack.pop();
            //System.out.println("Removing " + key + " at line " + var.line_declared);
            symTablePreserved.add(var);

            if(stack.isEmpty())
                symTable.remove(key);
            else
                symTable.put(key, stack);
        }        
    }
    
    private void printPreserved() {
        //Stack<Vars> stack = new Stack<Vars>();
        Vars var = new Vars();
        //Integer duplicate checking
        int prev = -1;
        int counter = 1;
        int size = 0;
        String current = null;
        
        while(!(symTablePreserved.isEmpty()))
        {
            size = 0;
            var = symTablePreserved.remove();
                
            System.out.println(var.ID);
            System.out.println("  declared on line " + var.line_declared + 
                    " at nesting depth " + var.nesting_depth);
            if(var.assigned.isEmpty())
            {
                System.out.println("  never assigned");
            }
            else
            {
                System.out.print("  assigned to on:");
                for(Integer x : var.assigned)
                {
                    if(var.ID != current)
                    {
                        prev = -1;
                        counter = 1;
                        current = var.ID;
                    }
                    
                    if(x == prev && size < (var.assigned.size()-1))
                    {
                         counter++;
                    }
                    else if(x != prev && counter == 1)
                    {
                         System.out.print(" " + x);  
                    }
                    else if(x != prev && counter > 1)
                    {
                         System.out.print("(" + counter + ")");
                         System.out.print(" " + x);
                         counter = 1;
                    }
                    else
                    {
                         counter++;
                         System.out.print("(" + counter + ")");
                    }
               
                     prev = x;
                     size++;
                 }
                 System.out.println();
            }
            
            if(var.used.isEmpty())
            {
                System.out.println("  never used");
            }
            else
            {
                System.out.print("  used on:");
            
                prev = -1;
                counter = 1;
                size = 0;
                current = null;
            
                for(Integer y : var.used)
                {
                    if(var.ID != current)
                    {
                        prev = -1;
                        counter = 1;
                        current = var.ID;
                    }
               
                    if(y == prev && size < (var.used.size()-1))
                    {
                         counter++;
                    }
                    else if(y != prev && counter == 1)
                    {
                         System.out.print(" " + y);  
                    }
                    else if(y != prev && counter > 1)
                    {
                         System.out.print("(" + counter + ")");
                         System.out.print(" " + y);
                         counter = 1;
                    }
                    else
                    {
                        counter++;
                        System.out.print("(" + counter + ")");
                    }
               
                     prev = y;
                     size++;
                 }
                 System.out.println();
            }
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
        ArrayList<String> sentinel = new ArrayList<String>();
        
        while( is(TK.ID) ) {
            if(!(sentinel.contains(tok.string)))
            {
                sentinel.add(tok.string);
                updateSymbols();
            }
            else
            {
                System.out.println("variable " + tok.string + " is redeclared on line " + tok.lineNumber);
                //updateSymbols();//even though symbol is redeclared it is still used
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
            invalid = false;
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
            currentDepth.put(depth, null);
            x_if();
            depth--;
        }
        else if(is(TK.DO))
        {
            depth++;
            currentDepth.put(depth, null);
            x_do();
            depth--;
        }
        else
        {
            depth++;
            currentDepth.put(depth, null);
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
            scan();
            simple();
        }
    }
    
    private void simple() {
        term();

        while(is(TK.PLUS) || is(TK.MINUS))
        {
            scan();
            term();
        }
    }
    
    private void term() {
        factor();

        while(is(TK.TIMES) || is(TK.DIVIDE))
        {
            scan();
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
            {
                System.out.println("undeclared variable " + tok.string +" on line " + tok.lineNumber);
                System.exit(1);
            }
            variableUsed(tok);
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