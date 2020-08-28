import java.math.BigInteger;
import java.util.Random;

public class Encryptor {

    public BigInteger n;
    /**
     * nsquare = n*n
     */
    public BigInteger nsquare;
    /**
     * a random integer in Z*_{n^2} where gcd (L(g^lambda mod n^2), n) = 1.
     */
    private BigInteger g;
    /**
     * number of bits of modulus
     */
    private int bitLength;


    public Encryptor(int bitLength, BigInteger g ,BigInteger n, BigInteger nsquare){
        this.bitLength = bitLength;
        this.g = g;
        this.n = n;
        this.nsquare = nsquare;
    }

    public BigInteger enc (BigInteger m){
        BigInteger r = new BigInteger(bitLength, new Random());
        return g.modPow(m, nsquare).multiply(r.modPow(n, nsquare)).mod(nsquare);
    }
}
