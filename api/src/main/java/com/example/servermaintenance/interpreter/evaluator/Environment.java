package com.example.servermaintenance.interpreter.evaluator;

import java.util.HashMap;

public class Environment {
    private final HashMap<String, EvalObject> env;

    public Environment() {
        env = new HashMap<>();
    }

    public void put(String key, EvalObject value) {
        env.put(key, value);
    }

    public void putInteger(String key, int value) {
        env.put(key, new EvalInteger(value));
    }

    public void putString(String key, String value) {
        env.put(key, new EvalString(value));
    }

    public EvalObject get(String key) {
        return env.get(key);
    }

    public EvalObject getOrDefault(String key, EvalObject other) {
        return env.getOrDefault(key, other);
    }
}
