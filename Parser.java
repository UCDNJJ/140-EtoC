/* *** This file is given as part of the programming assignment. *** */
import java.util.*; 

public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    List<String> relop = Arrays.asList("=","/=","<",">","<=",">=");
    List<String> addop = Arrays.asList("+","-");
    List<String> multop = Arrays.asList("*","/");
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
        // you'll need to add some code here
        if(is(TK.VAR))
          declarations();
        //System.out.println("In block tok =" + tok);
        //scan();
        //System.out.println("2nd block tok =" + tok);
        while(is(TK.ID) || is(TK.PRINT) || is(TK.IF) ||
              is(TK.DO) || is(TK.FA))
          statement();
    }

    private void declarations() {//if this is called, could have error in block
        mustbe(TK.VAR);
        while( is(TK.ID) ) {
            scan();
        }
        mustbe(TK.RAV);
    }
    
    private void statement() {
        scan();//get next token, while loop in block checked for correct 
               //first(stmt)
               //this scan is causing an issue with print since it is 
               //skipping over the print terminal and going straight to 
               //the ID being printed thus causing the else case to be 
               //called and exit the program early. 
        System.out.println("In stmt, token =" + tok);
        if(is(TK.ASSIGN))
        {
          //System.out.println("In statement tok =" + tok);
          assign();
        }
        else if(is(TK.PRINT))
        {
          print();
        }
        else if(is(TK.IF))
        {
          pif();
        }
        else if(is(TK.DO))
        {
          pdo();
        }
        else if(is(TK.FA))
        {
          pfa();
        }
        else
        {
          System.exit(1);
        }
    }

    private void assign() {
        //System.out.println("Token =" + tok);
        expr();
    }

    private void print() {
        mustbe(TK.PRINT);
        expr();
    }

    private void pif() {
        mustbe(TK.IF);
        System.out.println("In pif");
        guarded_commands();
        mustbe(TK.FI);
    }
 
    private void pdo() {
        mustbe(TK.DO);
        guarded_commands();
        mustbe(TK.OD);
    }

    private void pfa() {
        mustbe(TK.FA);
        scan();
        mustbe(TK.ID);
        scan();
        mustbe(TK.ASSIGN);
        expr();
        mustbe(TK.TO);
        expr();
        if(is(TK.ST))
        {
          expr();
        }
        else
          commands();

        mustbe(TK.AF);
    }

    private void guarded_commands() {
        guarded_command();//assuming a new token is created

        while(is(TK.BOX));
          guarded_command();

        if(is(TK.ELSE))
          commands();        
    }
 
    private void guarded_command() {
        expr();
        commands();//for the moment assuming commands sets a new token
    }

    private void commands() {
        mustbe(TK.ARROW);
        scan();
        block();//will this return a new token or must scan be called again?
    }

    private void expr() { //expr will scan next token after successful run
        simple();

        if(relop.contains(tok))
          simple();
     
    }

    private void simple() {
        term();
       
        while(addop.contains(tok))
          term();
    }

    private void term() {
        factor();
  
        while(multop.contains(tok))
          factor();
    }

    private void factor() {
        scan();
        System.out.println("Token at factor =" + tok);
        if(is(TK.LPAREN))
        {
          scan();
          expr();
          scan();
          mustbe(TK.RPAREN);
          scan();
        }
        else if(is(TK.ID))
        {
          scan();
        }
        else if(is(TK.NUM))
        {
          scan();
          //System.out.println("Factor goes to num");
        }
        else
        {
          System.err.println("Token does not meet grammar req. for factor");
          System.exit(1);
        }
        
        System.out.println("Returned Token from factor =" + tok); 
    }
     
    // you'll need to add a bunch of methods here

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
