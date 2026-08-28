package nl.kmartin.dartsmatcherapi.features.x01.x01checkout.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import nl.kmartin.dartsmatcherapi.features.dartboard.model.Dart;

import java.util.ArrayList;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class X01Checkout {
    private int checkout;
    private int minDarts;
    private ArrayList<Dart> suggested;
}
