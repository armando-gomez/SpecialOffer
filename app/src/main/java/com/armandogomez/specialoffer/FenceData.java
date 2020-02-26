package com.armandogomez.specialoffer;

import androidx.annotation.NonNull;

import java.io.Serializable;

public class FenceData implements Serializable {
	private String id;
	private double lat;
	private double lon;
	private String address;
	private float radius;
	private int type;
	private String fenceColor;

	FenceData(String id, double lat, double lon, String address, float radius, int type, String fenceColor) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.address = address;
		this.radius = radius;
		this.type = type;
		this.fenceColor = fenceColor;
	}

	public String getId() {
		return id;
	}

	public String getAddress() {
		return address;
	}

	public float getRadius() {
		return radius;
	}

	public int getType() {
		return type;
	}

	public String getFenceColor() {
		return fenceColor;
	}

	public double getLat() {
		return lat;
	}

	public double getLon() {
		return lon;
	}

	@NonNull
	@Override
	public String toString() {
		return "FenceData{" +
				"id='" + id + '\'' +
				", lat=" + lat +
				", lon=" + lon +
				", address='" + address + '\'' +
				", radius=" + radius +
				", type=" + type +
				", fenceColor='" + fenceColor + '\'' +
				'}';
	}
}
