# frequent_itemset
Privacy Preserving Frequent ItemSet Generation
Recent Technological advances, such as smart speakers, IP cameras and other network-connected smart home devices, have significantly increased the capability of commercial companies to store the personal data of consumers.This has raised concerns that this personal data will be viewed by unauthorized people, or even sold to other companies for a profit. In order to deal with these concerns, several privacy preserving data mining methods have been proposed in recent years.
In this Work, two privacy preserving algorithms for finding the frequent Itemsets in a vertically partitioned database were compared, both of which are based on dot product computation. These algorithms are:

1)	The Vaidya-Clifton Private Scalar Product Protocol.  
2)	The Homomorphic encryption based scalar product protocol proposed by Goethals et al. 

Due to the nature of the scalar dot product, both algorithms are generally limited to two-party interaction, and therefore were examined in a two-party setting, using 2 different databases and 3 values of support.  

The code is written in Java using the following dependencies:
1)	The Two-party communication is facilitated by Akka .
2)	The homomorphic encryption is based on the Paillier Cryptosystem. 
