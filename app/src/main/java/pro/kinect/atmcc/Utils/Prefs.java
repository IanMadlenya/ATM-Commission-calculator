package pro.kinect.atmcc.Utils;

import net.orange_box.storebox.annotations.method.KeyByString;

public interface Prefs {

    @KeyByString("key_central_exchange_rate")
    void setCentralExchangeRate(double value);

    @KeyByString("key_central_exchange_rate")
    double getCentralExchangeRate();

}
