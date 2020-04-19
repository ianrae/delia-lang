package org.delia.other.sysdesign;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class BuilderTests {
	
	public interface Room {
		//complicated code dealing with many parameters...
	}
	public static class Bedroom implements Room {
		//complicated code dealing with many parameters...
	}
	public static class Bathroom implements Room {
		//complicated code dealing with many parameters...
	}
	public static class House {
		private List<Room> rooms;
		
		public House(List<Room> rooms) {
			this.rooms = rooms;
		}
	}
	
	public interface HouseBuilder {
		HouseBuilder bathrooms(int numberOfBathrooms);
		HouseBuilder bedrooms(int numberOfBedrooms);
		House build();
	}
	
	public static class MyHouseBuilder implements HouseBuilder {
		private int numBedrooms;
		private int numBathrooms;

		@Override
		public HouseBuilder bathrooms(int numberOfBathrooms) {
			this.numBathrooms = numberOfBathrooms;
			return this;
		}

		@Override
		public HouseBuilder bedrooms(int numberOfBedrooms) {
			this.numBedrooms = numberOfBedrooms;
			return this;
		}
		
		@Override
		public House build() {
			List<Room> rooms = new ArrayList<>();
			for(int i = 0; i < numBedrooms; i++) {
				rooms.add(new Bedroom());
			}
			for(int i = 0; i < numBathrooms; i++) {
				rooms.add(new Bathroom());
			}
			House house = new House(rooms);
			return house;
		}
		
	}
	
	
	
	@Test
	public void test() {
		HouseBuilder builder = new MyHouseBuilder();
		builder.bathrooms(2).bedrooms(4);
		House house = builder.build();
		
	}
}
