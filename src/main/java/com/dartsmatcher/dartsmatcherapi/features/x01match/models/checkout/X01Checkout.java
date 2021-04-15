package com.dartsmatcher.dartsmatcherapi.features.x01match.models.checkout;

import com.dartsmatcher.dartsmatcherapi.features.match.Dart;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Checkout {

	private int checkout;

	private int minDarts;

	private ArrayList<Dart> suggested;

}
