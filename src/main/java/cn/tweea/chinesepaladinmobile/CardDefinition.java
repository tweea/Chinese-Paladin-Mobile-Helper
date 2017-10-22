/*
 * 版权所有 2017 Tweea。
 * 保留所有权利。
 */
package cn.tweea.chinesepaladinmobile;

import java.util.EnumMap;
import java.util.Map;

public class CardDefinition {
	private final String name;

	private final CardGrade grade;

	private final Map<CardDependencyType, CardDefinition> dependencies;

	private int 云裳数量;

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
}
