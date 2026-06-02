package com.ibitway.fmradio.models

import com.google.gson.annotations.SerializedName

data class RadioResponse(

	@field:SerializedName("broadcast")
	val broadcast: Broadcast? = null,

	@field:SerializedName("endpoints")
	val endpoints: Endpoints? = null,

	@field:SerializedName("date_time")
	val dateTime: String? = null,

	@field:SerializedName("timezone")
	val timezone: String? = null,

	@field:SerializedName("success")
	val success: Boolean? = null,

	@field:SerializedName("station_url")
	val stationUrl: String? = null,

	@field:SerializedName("language")
	val language: Any? = null,

	@field:SerializedName("schedule_url")
	val scheduleUrl: String? = null,

	@field:SerializedName("updated")
	val updated: String? = null,

	@field:SerializedName("stream_url")
	val streamUrl: String? = null,

	@field:SerializedName("timestamp")
	val timestamp: String? = null
)

data class NextShow(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("split")
	val split: Boolean? = null,

	@field:SerializedName("start")
	val start: String? = null,

	@field:SerializedName("show")
	val show: Show? = null,

	@field:SerializedName("end")
	val end: String? = null,

	@field:SerializedName("override")
	val override: Boolean? = null,

	@field:SerializedName("day")
	val day: String? = null,

	@field:SerializedName("encore")
	val encore: Boolean? = null
)

data class Broadcast(

	@field:SerializedName("current_playlist")
	val currentPlaylist: Boolean? = null,

	@field:SerializedName("next_show")
	val nextShow: NextShow? = null,

	@field:SerializedName("current_show")
	val currentShow: CurrentShow? = null
)

data class CurrentShow(

	@field:SerializedName("date")
	val date: String? = null,

	@field:SerializedName("split")
	val split: Boolean? = null,

	@field:SerializedName("start")
	val start: String? = null,

	@field:SerializedName("show")
	val show: Show? = null,

	@field:SerializedName("end")
	val end: String? = null,

	@field:SerializedName("override")
	val override: Boolean? = null,

	@field:SerializedName("day")
	val day: String? = null,

	@field:SerializedName("encore")
	val encore: Boolean? = null
){
	fun getTime():String{
		return "$day $start - $end"
	}
}

data class Show(

	@field:SerializedName("website")
	val website: String? = null,

	@field:SerializedName("languages")
	val languages: List<Any?>? = null,

	@field:SerializedName("hosts")
	val hosts: List<Any?>? = null,

	@field:SerializedName("image_url")
	val imageUrl: String? = null,

	@field:SerializedName("url")
	val url: String? = null,

	@field:SerializedName("producers")
	val producers: List<Any?>? = null,

	@field:SerializedName("feed")
	val feed: String? = null,

	@field:SerializedName("route")
	val route: String? = null,

	@field:SerializedName("avatar_url")
	val avatarUrl: String? = null,

	@field:SerializedName("genres")
	val genres: List<Any?>? = null,

	@field:SerializedName("name")
	val name: String = "",

	@field:SerializedName("id")
	val id: Int? = null,

	@field:SerializedName("slug")
	val slug: String? = null,

	@field:SerializedName("latest")
	val latest: String? = null
)

data class Endpoints(

	@field:SerializedName("broadcast")
	val broadcast: String? = null,

	@field:SerializedName("schedule")
	val schedule: String? = null,

	@field:SerializedName("shows")
	val shows: String? = null,

	@field:SerializedName("languages")
	val languages: String? = null,

	@field:SerializedName("genres")
	val genres: String? = null,

	@field:SerializedName("station")
	val station: String? = null
)
