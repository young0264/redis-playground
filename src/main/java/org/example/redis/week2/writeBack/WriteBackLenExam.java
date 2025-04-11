//package org.example.redis.week2.writeBack;
//
//public class WriteBackLenExam {
//
//    private final ProductRepository productRepository;
//    private final RedissonClient redissonClient;
//
//    public ProductWriteBehindService(ProductRepository productRepository, RedissonClient redissonClient) {
//        this.productRepository = productRepository;
//        this.redissonClient = redissonClient;
//
//// write-behind 설정
//        RMapCache<String, Product> cache = redissonClient.getMapCache("productCache");
//        cache.setWriteBehindDelay(5, TimeUnit.SECONDS);
//        cache.setWriteBehindBatchSize(100);
//        cache.setWriteBehindStore(new MapWriter<>() {
//            public void write(Map<String, Product> batch) {
//                productRepository.saveAll(batch.values());
//            }
//
//            public void delete(Collection<String> keys) {
//                productRepository.deleteAllById(keys);
//            }
//        });
//    }
//
//    public void saveProduct(Product product) {
//        RMapCache<String, Product> cache = redissonClient.getMapCache("productCache");
//        cache.put(product.getId(), product);
//    }
//}