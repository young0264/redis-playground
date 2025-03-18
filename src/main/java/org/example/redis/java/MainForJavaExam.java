package org.example.redis.java;

public class MainForJavaExam {
    public static void main(String[] args) throws InterruptedException {

        String redisUri = "redis://localhost:6379";
        RedisManager redisManager = new RedisManager(redisUri);

        //== Redis 기본 연산 테스트 ==//

        // List 테스트
        redisManager.pushToList("myList","Task1");
        redisManager.pushToList("myList","Task2");
        System.out.println("List 조회: " + redisManager.getList("myList"));

        // Set 테스트
        redisManager.addToSet("mySet", "pican");
        redisManager.addToSet("mySet", "walnut");
        redisManager.addToSet("mySet", "pican");
        System.out.println("Set 조회: "+redisManager.getSet("mySet"));

        // Sorted Set 테스트
        redisManager.addToSortedSet("mySortedSet", 3, "TaskA");
        redisManager.addToSortedSet("mySortedSet", 2, "TaskB");
        System.out.println("Sorted Set 조회: "+redisManager.getSortedSet("mySortedSet"));

        // Hash 테스트
        redisManager.setHash("myHash"," name", "Alice");
        redisManager.setHash("myHash"," age", "30");
        redisManager.setHash("myHash"," name", "EuiYoung");

        System.out.println("\n");
        // String 테스트
        redisManager.setString("myKey", "Hello Redis!");
        System.out.println("myKey 값: " + redisManager.getString("myKey"));

        redisManager.deleteKey("myKey");
        System.out.println("myKey 삭제 후 값: " + redisManager.getString("myKey"));

        redisManager.setString("myKey", "Hello Redis2!");
        System.out.println("myKey 만료 전: " + redisManager.getString("myKey"));
        redisManager.setExpiration("myKey", 5); // 머냐 이건
        Thread.sleep(5000);
        System.out.println("myKey 만료 후: " + redisManager.getString("myKey"));
        redisManager.close();
        System.out.println("\n");

        // 할 일 관리 시스템 테스트
        TodoManager todoManager = new TodoManager(redisUri);
        todoManager.addTodo("보고서 작성");
        todoManager.addTodo("이메일 보내기");
        todoManager.printTodos();

        todoManager.completeTodo(); // 완료된 일 제거
        todoManager.printTodos();

        todoManager.setExpiration(5); // 5초 후 자동 삭제
        Thread.sleep(5000);
        System.out.println("========== 5초 후 자동 삭제 ==========");
        todoManager.printTodos();

        todoManager.close();

    }
}