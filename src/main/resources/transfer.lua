-- KEYS[1] = fromAccount
-- KEYS[2] = toAccount
-- ARGV[1] = amount

local fromBalance = tonumber(redis.call('GET', KEYS[1]))
local toBalance = tonumber(redis.call('GET', KEYS[2]))
local amount = tonumber(ARGV[1])

if fromBalance == nil or toBalance == nil then
  return redis.error_reply("Account does not exist")
end

if fromBalance < amount then
  return redis.error_reply("Insufficient funds")
end

redis.call('DECRBY', KEYS[1], amount)
redis.call('INCRBY', KEYS[2], amount)

return "OK"
