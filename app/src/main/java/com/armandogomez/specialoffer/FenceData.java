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
	private String website;
	private String offer;
	private String logo;
	private String code;

	FenceData(String id, double lat, double lon, String address, float radius, int type, String fenceColor, String website, String offer, String logo, String code) {
		this.id = id;
		this.lat = lat;
		this.lon = lon;
		this.address = address;
		this.radius = radius;
		this.type = type;
		this.fenceColor = fenceColor;
		this.website = website;
		this.offer = offer;
		this.logo = logo;
		this.code = code;
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

	public String getOffer() {
		return offer;
	}

	public String getWebsite() {
		return website;
	}

	public String getLogo() {
		return logo;
	}

	public String getCode() {
		return code;
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
