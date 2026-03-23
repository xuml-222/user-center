package xuml.study.com.common.utils;

/**
 * 雪花算法主键生成工具类
 */
public class SnowflakeIdUtils {
    // 起始时间戳（2020-01-01）
    private static final long START_STAMP = 1577808000000L;
    // 每一部分占用的位数
    private static final long SEQUENCE_BIT = 12; // 序列号占用位数
    private static final long MACHINE_BIT = 5;   // 机器标识占用位数
    private static final long DATACENTER_BIT = 5;// 数据中心占用位数

    // 每一部分的最大值
    private static final long MAX_DATACENTER_NUM = ~(-1L << DATACENTER_BIT);
    private static final long MAX_MACHINE_NUM = ~(-1L << MACHINE_BIT);
    private static final long MAX_SEQUENCE = ~(-1L << SEQUENCE_BIT);

    // 每一部分向左的位移
    private static final long MACHINE_LEFT = SEQUENCE_BIT;
    private static final long DATACENTER_LEFT = SEQUENCE_BIT + MACHINE_BIT;
    private static final long TIMESTAMP_LEFT = DATACENTER_LEFT + DATACENTER_BIT;

    private final long datacenterId;  // 数据中心
    private final long machineId;     // 机器标识
    private long sequence = 0L; // 序列号
    private long lastStamp = -1L;// 上一次时间戳

    private static final SnowflakeIdUtils instance = new SnowflakeIdUtils(1, 1);

    public static long nextId() {
        return instance.nextSnowflakeId();
    }

    public SnowflakeIdUtils(long datacenterId, long machineId) {
        if (datacenterId > MAX_DATACENTER_NUM || datacenterId < 0) {
            throw new IllegalArgumentException("datacenterId can't be greater than " + MAX_DATACENTER_NUM + " or less than 0");
        }
        if (machineId > MAX_MACHINE_NUM || machineId < 0) {
            throw new IllegalArgumentException("machineId can't be greater than " + MAX_MACHINE_NUM + " or less than 0");
        }
        this.datacenterId = datacenterId;
        this.machineId = machineId;
    }

    public synchronized long nextSnowflakeId() {
        long currStamp = getCurrentTime();
        if (currStamp < lastStamp) {
            throw new RuntimeException("Clock moved backwards. Refusing to generate id");
        }
        if (currStamp == lastStamp) {
            sequence = (sequence + 1) & MAX_SEQUENCE;
            if (sequence == 0L) {
                currStamp = getNextMill();
            }
        } else {
            sequence = 0L;
        }
        lastStamp = currStamp;
        return ((currStamp - START_STAMP) << TIMESTAMP_LEFT)
                | (datacenterId << DATACENTER_LEFT)
                | (machineId << MACHINE_LEFT)
                | sequence;
    }

    private long getNextMill() {
        long mill = getCurrentTime();
        while (mill <= lastStamp) {
            mill = getCurrentTime();
        }
        return mill;
    }

    private long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 生成指定长度的主键字符串，不足左侧补零
     */
    public static String nextIdFixedLength(int length) {
        String idStr = String.valueOf(nextId());
        if (idStr.length() >= length) {
            return idStr;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length - idStr.length(); i++) {
            sb.append('0');
        }
        sb.append(idStr);
        return sb.toString();
    }

    public static void main(String[] args) {
        System.out.printf(nextIdFixedLength(9));
    }
} 