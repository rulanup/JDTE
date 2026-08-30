package com.jdte.common.upgrades;

/**
 * 机器自行声明可安装的标准升级集合，供跨模组机器复用 JDTE 升级体系时
 * 限制 {@link UpgradeHelper#isUpgradeCompatible} 的结果（参照 ICreativeGreenhouse 模式）。
 */
public interface SelectiveUpgradeMachine {
    /** 返回该机器是否允许安装指定类型的标准升级。 */
    boolean isUpgradeAllowed(UpgradeType type);
}
