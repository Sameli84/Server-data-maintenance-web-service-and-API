package com.example.servermaintenance.interpreter;

import com.example.servermaintenance.interpreter.evaluator.Environment;
import com.example.servermaintenance.interpreter.evaluator.Evaluator;
import com.example.servermaintenance.interpreter.lexer.Lexer;
import com.example.servermaintenance.interpreter.parser.Parser;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.security.RolesAllowed;


@Controller
@AllArgsConstructor
public class ReplController {
    @RolesAllowed("ADMIN")
    @GetMapping("/repl")
    public String getReplPage() {
        return "repl";
    }

    @RolesAllowed("ADMIN")
    @GetMapping("/repl-exec")
    public String execRepl(@RequestParam String statement, Model model) {
        var lexer = new Lexer(statement);
        var parser = new Parser(lexer);

        var program = parser.parseProgram();

        if (parser.getErrors().size() != 0) {
            model.addAttribute("error", parser.getErrors());
            return "repl-out";
        }

        var env = new Environment();
        env.putInteger("id", 1);
        var evaluated = Evaluator.eval(program, env);
        if (evaluated != null) {
            model.addAttribute("out", evaluated.toString());
        }
        return "repl-out";
    }
}
