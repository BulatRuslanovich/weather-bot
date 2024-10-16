package com.getname.dto;

import lombok.Data;

@Data
public class WeatherResponse {

	private String name;

	private Main main;

	private Weather[] weather;

	private Wind wind;

	private Sys sys;

	@Data
	public static class Main {
		private String temp;

		private String humidity;

		private String pressure;
	}

	@Data
	public static class Weather {
		private String main;
	}

	@Data
	public static class Wind {
		private String speed;
	}

	@Data
	public static class Sys {
		private long sunrise;

		private long sunset;
	}
}