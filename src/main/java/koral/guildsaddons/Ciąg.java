package koral.guildsaddons;

import com.google.common.collect.Lists;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

public class Ciąg<T> {
    public static class Krotka<T1, T2>{
        public T1 a;
        public T2 b;

        public Krotka() {}
        public Krotka(T1 a, T2 b) {
            this.a = a;
            this.b = b;
        }
    }
    static final Random random = new Random();
    List<Krotka<Integer, T>> lista = Lists.newArrayList();
    int suma = 0;

    public Ciąg() {}
    public Ciąg(List<Krotka<Integer, T>> lista) {
        this.lista = lista;
        przetwórz();
    }

    private void przetwórz() {
        List<Krotka<Integer, T>> nowa = Lists.newArrayList(lista.iterator());
        this.lista = Lists.newArrayList();
        for (Krotka<Integer, T> krotka : nowa)
            dodaj(krotka.b, krotka.a);
    }

    public void wyczyść() {
        lista.clear();
        suma = 0;
    }
    public int wielkość() {
        return lista.size();
    }

    public List<T> klucze() {
        List<T> lista = Lists.newArrayList();

        this.lista.forEach(k -> lista.add(k.b));

        return lista;
    }

    public HashMap<T, Double> szanse() {
        HashMap<T, Double> mapa = new HashMap<>();

        int ost = 0;
        int akt = 0;

        for (Krotka<Integer, T> k : lista) {
            akt = k.a - ost;
            ost = k.a;
            mapa.put(k.b, mapa.getOrDefault(k.b, 0.0) + (akt / ((double) suma)));
        }


        return mapa;
    }

    public void dodaj(T co, int szansa) {
        lista.add(new Krotka<>(szansa + suma, co));
        suma += szansa;
    }

    public T znajdz(int numer) {
        return wyszukajBinarnieP(numer, lista, k -> (double)k.a).b;
    }
    public T losuj() {
        return znajdz(random.nextInt(suma) + 1);
    }

    /**
     *
     * @param <T> typ objektów listy
     * @param numer szukany numer (rezulatat "wartość")
     * @param posortowanaLista posortowana rosnąco Lista według wartości wynikających z "wartość"
     * @param wartość funkcja zwracająca wartość dla obiektu listy
     * @return zwraca obiekt którego wartość == "numer", jesli
     * jeśli na osi punkt od "numer" nie istnieje, zwrócony zostanie istniejący punkt po jego prawej stronie
     */
    public static <T> T wyszukajBinarnieP(double numer, List<T> posortowanaLista, Function<T, Double> wartość) {
        return posortowanaLista.get(wyszukajBinarniePIndex(numer, posortowanaLista, wartość));
    }
    public static <T> int wyszukajBinarniePIndex(double numer, List<T> posortowanaLista, Function<T, Double> wartość) {
        int l = 0;
        int r = posortowanaLista.size() - 1;

        while (l < r) {
            int s = l + ((r - l) / 2);
            double w = wartość.apply(posortowanaLista.get(s));

            if (w < numer)
                l = s + 1;
            else
                r = s;
        }
        return l;
    }
}
