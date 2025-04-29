package org.example.redis.week4.bankAccountSystem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class LuaScriptLoader {
    public static String loadScript(String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (IOException e) {
            throw new RuntimeException("Fail path : " + path, e);
        }
    }
}
