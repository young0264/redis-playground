package org.example.redis.java;

public class RedisTester {
    private final RedisManager redisManager;
    private final TodoManager todoManager;

    public RedisTester(String redisUri) {
        this.redisManager = new RedisManager(redisUri);
        this.todoManager = new TodoManager(redisUri);
    }

    public void runTests() throws InterruptedException {
        testListOperations();       // List
        testSetOperations();        // Set
        testSortedSetOperations();  // Sorted Set
        testHashOperations();       // Hash
        System.out.println("\n");
        testStringOperations();     // String
        System.out.println("\n");
        testTodoManager();
        closeConnections();
    }

    /** List */
    private void testListOperations() {
        redisManager.pushToList("myList", "Task1");
        redisManager.pushToList("myList", "Task2");
        log("List 조회", redisManager.getList("myList"));
    }

    /** Set */
    private void testSetOperations() {
        redisManager.addToSet("mySet", "pican");
        redisManager.addToSet("mySet", "walnut");
        redisManager.addToSet("mySet", "pican"); // 중복 방지 확인
        log("Set 조회", redisManager.getSet("mySet"));
    }

    /** Sorted Set */
    private void testSortedSetOperations() {
        redisManager.addToSortedSet("mySortedSet", 3, "TaskA");
        redisManager.addToSortedSet("mySortedSet", 2, "TaskB");
        log("Sorted Set 조회", redisManager.getSortedSet("mySortedSet"));
    }

    /** Hash */
    private void testHashOperations() {
        redisManager.setHash("myHash", "name", "Alice");
        redisManager.setHash("myHash", "age", "30");
        redisManager.setHash("myHash", "name", "EuiYoung");
        log("Hash 조회", redisManager.getHash("myHash"));
    }

    /** String */
    private void testStringOperations() throws InterruptedException {
        redisManager.setString("myKey", "Hello Redis!");
        log("myKey 값", redisManager.getString("myKey"));

        redisManager.deleteKey("myKey");
        log("myKey 삭제 후. 값", redisManager.getString("myKey"));

        redisManager.setString("myKey", "Hello Redis2!");
        log("myKey 만료 전 값", redisManager.getString("myKey"));
        redisManager.setExpiration("myKey", 5);
        Thread.sleep(5000);
        log("myKey 만료 후 값", redisManager.getString("myKey"));
    }

    /** Todo_Manager */
    private void testTodoManager() throws InterruptedException {
        todoManager.addTodo("보고서 작성");
        todoManager.addTodo("이메일 보내기");
        todoManager.printTodos();

        todoManager.completeTodo(); // 완료된 일 제거
        todoManager.printTodos();

        todoManager.setExpiration(5); // 5초 후 자동 삭제
        Thread.sleep(5000);
        log("========== 5초 후 전체 자동 삭제 ==========");
        todoManager.printTodos();
    }

    /** connection */
    private void closeConnections() {
        redisManager.close();
        todoManager.close();
    }

    private void log(String message, Object data) {
        System.out.println(message + ": " + data);
    }

    private void log(String message) {
        System.out.println(message);
    }
}
