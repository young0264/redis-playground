package org.example.week2.writeBack;

import org.example.redis.week2.FakeProductRepository;
import org.example.redis.week2.Product;
import org.example.redis.week2.ProductService;
import org.example.redis.week2.UpdateProductCommand;
import org.example.redis.week2.writeBack.WriteBackExam;
import org.junit.jupiter.api.*;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WriteBackExamTest {
    private RedissonClient redisson;
    private FakeProductRepository db;
    private ProductService productService;
    private WriteBackExam writeBackExamService;
    private BlockingQueue<UpdateProductCommand> updateQueue;

    @BeforeEach
    void setup() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379");
        redisson = Redisson.create(config);
        db = new FakeProductRepository();
        updateQueue = new LinkedBlockingQueue<>();
        productService = new ProductService(redisson, db, updateQueue);
        writeBackExamService = new WriteBackExam(redisson, db, updateQueue);
    }

    @Test
    @DisplayName("Write-Back exam test")
    void testWriteBack() {
        Product updated = new Product("3", "캐시전용상품", 9999);
        writeBackExamService.updateProduct("3", updated);

        Product cached = productService.getProduct("3");
        assertEquals("캐시전용상품", cached.name());

        // DB에는 아직 반영되지 않았을 수 있음
        Product fromDb = db.findById("3");
        Assertions.assertNotEquals("캐시전용상품", fromDb.name());
    }

//    @Test
//    @DisplayName("큐에 저장된 작업이 Dispatcher를 통해 DB에 반영되어야 한다")
//    void dispatcherShouldProcessQueueAndCallDbUpdate() throws InterruptedException {
//    }
}