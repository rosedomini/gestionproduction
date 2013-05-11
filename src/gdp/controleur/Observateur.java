package gdp.controleur;


public interface Observateur {
	public void onNotification(Observable notifier, String notification);
}
