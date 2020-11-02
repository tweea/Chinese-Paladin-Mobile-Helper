/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

import java.util.EnumMap;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class CardDefinition {
    private final String name;

    private final CardGrade grade;

    private final Map<CardDependencyType, CardDefinition> dependencies;

    private int 云裳数量;

    private int 云裳部件数量;

    public CardDefinition(String name, CardGrade grade) {
        this.name = name;
        this.grade = grade;
        this.dependencies = new EnumMap<>(CardDependencyType.class);
    }

    public String getName() {
        return name;
    }

    public CardGrade getGrade() {
        return grade;
    }

    public Map<CardDependencyType, CardDefinition> getDependencies() {
        return dependencies;
    }

    public int get云裳数量() {
        return 云裳数量;
    }

    public void set云裳数量(int 云裳数量) {
        this.云裳数量 = 云裳数量;
    }

    public int get云裳部件数量() {
        return 云裳部件数量;
    }

    public void set云裳部件数量(int 云裳部件数量) {
        this.云裳部件数量 = 云裳部件数量;
    }

    @Override
    public String toString() {
        Map<CardDependencyType, String> dependencyNames = new EnumMap<>(CardDependencyType.class);
        for (Map.Entry<CardDependencyType, CardDefinition> dependencyEntry : dependencies.entrySet()) {
            dependencyNames.put(dependencyEntry.getKey(), dependencyEntry.getValue().name);
        }
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE).append("name", name);
        if (grade == null) {
            builder.append("grade", 0);
        } else {
            builder.append("grade", grade.getGrade());
        }
        return builder.append("dependencies", dependencyNames).append("云裳数量", 云裳数量).append("云裳部件数量", 云裳部件数量).toString();
    }
}
