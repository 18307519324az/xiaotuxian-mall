local stockKey = KEYS[1]
local requestKey = KEYS[2]
local deductCount = tonumber(ARGV[1])
local requestId = ARGV[2]
local requestTtl = tonumber(ARGV[3])

if redis.call('exists', requestKey) == 1 then
    return 2
end

local stock = redis.call('get', stockKey)
if stock == false then
    return -1
end

stock = tonumber(stock)
if stock < deductCount then
    return 0
end

redis.call('decrby', stockKey, deductCount)
redis.call('set', requestKey, requestId, 'EX', requestTtl)
return 1
