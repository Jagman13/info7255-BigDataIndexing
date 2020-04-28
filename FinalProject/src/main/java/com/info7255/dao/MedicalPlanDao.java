package com.info7255.dao;

import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Repository;

import redis.clients.jedis.Jedis;
/**
 * Created by jagman on Feb, 2020
 **/
@Repository
public class MedicalPlanDao {

    // Adds specified member to the set value stored at a specified key
    public void addSetValue(String key, String value) {
        try (Jedis jedis = new Jedis("localhost")) {
            jedis.sadd(key, value);
        }
    }

    // Sets the specified hash field to the specified value
    public void hSet(String key, String field, String value ) {
        try (Jedis jedis = new Jedis("localhost")) {
            jedis.hset(key, field, value);
        }
    }

    public boolean checkIfKeyExist(String key) {
        try (Jedis jedis = new Jedis("localhost")) {
            return jedis.exists(key);
        }
    }

    // Get all keys that matches the pattern
    public Set<String> getKeys(String pattern){
        try (Jedis jedis = new Jedis("localhost")) {
            return jedis.keys(pattern);
        }
    }

    // Return all members of the set value stored at the specified key
    public Set<String> sMembers(String key) {
        try (Jedis jedis = new Jedis("localhost")) {
            return jedis.smembers(key);
        }
    }

    // Get all fields and associated values in a hash
    public Map<String,String> getAllValuesByKey(String key) {
        try (Jedis jedis = new Jedis("localhost")) {
            return jedis.hgetAll(key);
        }
    }

    public String hGet(String key, String field) {
        try (Jedis jedis = new Jedis("localhost")) {
            return jedis.hget(key, field);
        }
    }

    // Delete keys
    public long deleteKeys(String[] keys) {
        try (Jedis jedis = new Jedis("localhost")) {
            return jedis.del(keys);
        }
    }

}
