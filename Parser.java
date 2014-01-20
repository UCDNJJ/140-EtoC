/* *** This file is given as part of the programming assignment. *** */
//Nelson Johansen
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

    private void program() {
        block();
    }

    private void block() {
        if(is(TK.VAR))
        {
          declarations();
        }
        
        statement_list();
    }

    private void declarations() {
        mustbe(TK.VAR);
        while( is(TK.ID) ) {
            scan();
        }
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
            assign();
        }
        else if(is(TK.PRINT))
        {
            print();
        }
        else if(is(TK.IF))
        {
            x_if();
        }
        else if(is(TK.DO))
        {
            x_do();
        }
        else
        {
            x_fa();
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
    
    private void expr() { //returns a new tok when complete, don't scan after
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
