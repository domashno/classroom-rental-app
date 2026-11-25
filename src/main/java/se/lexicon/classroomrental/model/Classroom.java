package se.lexicon.classroomrental.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Classroom {

	private final int id;
	private String name;
	private int capacity;
	private List<String> equipment;
	private boolean accessible;

	public Classroom(int id, String name, int capacity, List<String> equipment, boolean accessible) {
		this.id = id;
		setName(name);
		setCapacity(capacity);
		setEquipment(equipment);
		this.accessible = accessible;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public final void setName(String name) {
		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("Classroom name cannot be blank.");
		}
		this.name = name.trim();
	}

	public int getCapacity() {
		return capacity;
	}

	public final void setCapacity(int capacity) {
		if (capacity <= 0) {
			throw new IllegalArgumentException("Classroom capacity must be greater than zero.");
		}
		this.capacity = capacity;
	}

	public List<String> getEquipment() {
		return Collections.unmodifiableList(equipment);
	}

	public final void setEquipment(List<String> equipment) {
		if (equipment == null) {
			this.equipment = new ArrayList<>();
			return;
		}
		List<String> cleaned = new ArrayList<>();
		for (String item : equipment) {
			if (item != null && !item.isBlank()) {
				cleaned.add(item.trim());
			}
		}
		this.equipment = cleaned;
	}

	public boolean isAccessible() {
		return accessible;
	}

	public void setAccessible(boolean accessible) {
		this.accessible = accessible;
	}

	public boolean hasEquipment(String requiredEquipment) {
		if (requiredEquipment == null || requiredEquipment.isBlank()) {
			return true;
		}
		String target = requiredEquipment.trim().toLowerCase();
		return equipment.stream().anyMatch(eq -> eq.toLowerCase().contains(target));
	}

	public String formatSummary() {
		String eqText = equipment.isEmpty() ? "None" : String.join(", ", equipment);
		return String.format(
				"[#%d] %-24s | Cap: %2d | Accessible: %-3s | Equipment: %s",
				id, name, capacity, accessible ? "Yes" : "No", eqText
		);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Classroom classroom)) return false;
		return id == classroom.id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}
}