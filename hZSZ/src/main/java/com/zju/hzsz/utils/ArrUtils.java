package com.zju.hzsz.utils;

public class ArrUtils {
	public static <T> int indexOf(T[] arr, T v) {
		if (arr == null)
			return -2;
		for (int i = 0; i < arr.length; ++i) {
			if (arr[i].equals(v))
				return i;
		}
		return -1;
	}

	public static int indexOf(int[] arr, int v) {
		if (arr == null)
			return -2;
		for (int i = 0; i < arr.length; ++i) {
			if (arr[i] == v)
				return i;
		}
		return -1;
	}
}
