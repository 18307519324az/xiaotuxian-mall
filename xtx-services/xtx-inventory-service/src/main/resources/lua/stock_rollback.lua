local stockKey = KEYS[1]
local requestKey = KEYS[2]
local rollbackCount = tonumber(ARGV[1])

if redis.call('exists', requestKey) == 0 then
    return 0
end

redis.call('incrby', stockKey, rollbackCount)
redis.call('del', requestKey)
return 1
