package com.droidcrypt;


import java.util.Random;

public class common
{

	private Integer[] mats = {
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	109, 71, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	109, 79, 83, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	89, 127, 99, 69, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	95, 75, 121, 71, 109, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	71, 117, 127, 75, 89, 109, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	111, 83, 127, 97, 77, 117, 89, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	113, 111, 87, 93, 99, 73, 117, 123, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	89, 97, 115, 81, 77, 117, 87, 127, 123, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	95, 107, 109, 79, 117, 67, 121, 123, 103, 81, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	117, 71, 109, 79, 101, 115, 123, 81, 77, 95, 87, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	119, 73, 81, 125, 123, 103, 99, 127, 109, 69, 89, 107, 0, 0, 0, 0, 0, 0, 0, 0, 
	87, 127, 117, 81, 97, 67, 101, 93, 105, 109, 75, 115, 123, 0, 0, 0, 0, 0, 0, 0, 
	93, 107, 115, 95, 121, 81, 75, 99, 111, 85, 79, 119, 105, 65, 0, 0, 0, 0, 0, 0, 
	123, 85, 79, 87, 127, 65, 115, 93, 101, 111, 73, 119, 105, 99, 91, 0, 0, 0, 0, 0, 
	127, 99, 121, 111, 71, 109, 103, 117, 113, 65, 105, 87, 101, 75, 93, 123, 0, 0, 0, 0, 
	89, 93, 111, 117, 103, 127, 77, 95, 85, 105, 67, 69, 113, 123, 99, 75, 119, 0, 0, 0, 
	65, 99, 77, 85, 101, 91, 125, 103, 127, 111, 69, 93, 75, 95, 119, 113, 105, 115, 0, 0, 
	91, 117, 77, 107, 101, 127, 115, 83, 85, 119, 105, 113, 93, 71, 111, 121, 97, 73, 81, 0, 
	95, 111, 117, 83, 97, 75, 87, 127, 85, 93, 105, 115, 77, 101, 99, 89, 71, 121, 67, 123, 
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	247, 149, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	143, 187, 233, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	235, 141, 161, 207, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	219, 185, 151, 255, 197, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	251, 159, 217, 167, 221, 133, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	201, 143, 231, 251, 189, 169, 155, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	143, 245, 177, 253, 217, 163, 155, 197, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	233, 145, 219, 185, 231, 215, 173, 129, 243, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	139, 201, 177, 167, 213, 253, 227, 199, 185, 159, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	183, 145, 223, 199, 245, 139, 187, 157, 217, 237, 163, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	223, 145, 137, 219, 197, 243, 247, 189, 135, 181, 207, 235, 0, 0, 0, 0, 0, 0, 0, 0, 
	229, 205, 237, 187, 135, 241, 183, 163, 151, 243, 213, 137, 159, 0, 0, 0, 0, 0, 0, 0, 
	205, 165, 239, 211, 231, 247, 133, 227, 219, 189, 249, 185, 149, 129, 0, 0, 0, 0, 0, 0, 
	131, 213, 255, 207, 227, 221, 173, 185, 197, 147, 235, 247, 217, 143, 229, 0, 0, 0, 0, 0, 
	247, 139, 157, 223, 187, 147, 177, 249, 165, 153, 161, 227, 237, 255, 207, 197, 0, 0, 0, 0, 
	205, 139, 239, 183, 147, 187, 249, 225, 253, 163, 173, 233, 209, 159, 255, 149, 197, 0, 0, 0, 
	177, 173, 195, 137, 211, 249, 191, 135, 175, 155, 229, 215, 203, 225, 247, 237, 221, 227, 0, 0, 
	159, 189, 195, 163, 255, 147, 219, 247, 231, 157, 139, 173, 185, 197, 207, 245, 193, 241, 233, 0, 
	235, 179, 219, 253, 241, 131, 213, 231, 247, 223, 201, 193, 191, 249, 145, 237, 155, 165, 141, 173, 
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	339, 489, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	469, 441, 379, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	371, 439, 277, 479, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	413, 489, 443, 327, 357, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	509, 453, 363, 409, 425, 303, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	377, 337, 443, 487, 467, 421, 299, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	497, 349, 279, 395, 365, 427, 399, 297, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	435, 373, 395, 507, 441, 325, 279, 289, 319, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	301, 379, 509, 411, 293, 467, 455, 261, 343, 447, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	367, 289, 445, 397, 491, 279, 373, 315, 435, 473, 327, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	465, 379, 319, 275, 293, 407, 373, 427, 445, 497, 347, 417, 0, 0, 0, 0, 0, 0, 0, 0, 
	473, 401, 267, 311, 359, 347, 333, 441, 405, 381, 497, 463, 269, 0, 0, 0, 0, 0, 0, 0, 
	467, 283, 405, 303, 269, 337, 385, 441, 511, 361, 455, 355, 353, 311, 0, 0, 0, 0, 0, 0, 
	489, 311, 259, 287, 445, 471, 419, 345, 289, 391, 405, 411, 371, 457, 331, 0, 0, 0, 0, 0, 
	493, 427, 305, 309, 339, 447, 381, 335, 323, 423, 453, 457, 443, 313, 371, 353, 0, 0, 0, 0, 
	271, 301, 483, 401, 369, 367, 435, 329, 319, 473, 441, 491, 325, 455, 389, 341, 317, 0, 0, 0, 
	333, 311, 509, 319, 391, 441, 279, 467, 263, 487, 393, 405, 473, 303, 353, 337, 451, 365, 0, 0, 
	301, 477, 361, 445, 505, 363, 375, 277, 271, 353, 337, 503, 457, 357, 287, 323, 435, 345, 497, 0, 
	281, 361, 413, 287, 475, 359, 483, 351, 337, 425, 453, 423, 301, 309, 331, 499, 507, 277, 375, 471, 
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	519, 885, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	579, 943, 781, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	685, 663, 947, 805, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	959, 729, 679, 609, 843, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	959, 973, 793, 747, 573, 659, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	631, 559, 1023, 805, 709, 913, 979, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	607, 867, 731, 1013, 625, 973, 825, 925, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	743, 727, 851, 961, 813, 605, 527, 563, 867, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	863, 921, 943, 523, 653, 969, 563, 597, 753, 621, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	729, 747, 901, 839, 815, 935, 777, 641, 1011, 603, 973, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	581, 831, 659, 877, 781, 929, 1003, 1021, 655, 729, 983, 611, 0, 0, 0, 0, 0, 0, 0, 0, 
	873, 1013, 859, 887, 579, 697, 769, 927, 679, 683, 911, 753, 733, 0, 0, 0, 0, 0, 0, 0, 
	991, 767, 845, 977, 923, 609, 633, 769, 533, 829, 859, 759, 687, 657, 0, 0, 0, 0, 0, 0, 
	781, 663, 731, 829, 851, 941, 601, 997, 719, 675, 947, 939, 657, 549, 647, 0, 0, 0, 0, 0, 
	619, 879, 681, 601, 1015, 797, 737, 841, 839, 869, 931, 789, 767, 547, 823, 635, 0, 0, 0, 0, 
	855, 567, 591, 1019, 745, 945, 769, 671, 803, 799, 925, 701, 517, 653, 885, 731, 581, 0, 0, 0, 
	887, 643, 785, 611, 905, 669, 703, 1017, 575, 763, 625, 869, 731, 861, 847, 941, 933, 577, 0, 0, 
	867, 991, 1021, 709, 599, 741, 933, 921, 619, 789, 957, 791, 969, 525, 591, 763, 657, 683, 829, 0, 
	1009, 1003, 901, 715, 643, 803, 805, 975, 667, 619, 569, 769, 685, 767, 853, 671, 881, 907, 955, 523, 
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	1655, 1493, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	1859, 1481, 1119, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	1395, 1737, 1973, 1259, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	1339, 1067, 1679, 1641, 2021, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	1657, 1331, 1783, 2043, 1097, 1485, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	1611, 1141, 1849, 2001, 1511, 1359, 1245, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	1215, 1733, 1461, 2025, 1251, 1945, 1649, 1851, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	1275, 1373, 1841, 1509, 1631, 1737, 1055, 1891, 1041, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	1715, 1117, 1503, 2025, 1027, 1959, 1365, 1739, 1301, 1233, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	1101, 1127, 1145, 1157, 1195, 1747, 1885, 1527, 1325, 2033, 1935, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	1369, 1255, 1809, 1889, 1183, 1495, 1223, 1781, 2029, 1327, 1075, 1065, 0, 0, 0, 0, 0, 0, 0, 0, 
	1157, 1499, 1871, 1365, 1559, 1149, 1293, 1571, 1641, 1971, 1807, 1673, 2023, 0, 0, 0, 0, 0, 0, 0, 
	1929, 1533, 1135, 1359, 1547, 1723, 1529, 1107, 1273, 1879, 1709, 1141, 1897, 1161, 0, 0, 0, 0, 0, 0, 
	1861, 1801, 1675, 1699, 1103, 1665, 1657, 1287, 1459, 2047, 1181, 1835, 1085, 1377, 1511, 0, 0, 0, 0, 0, 
	1915, 1753, 1945, 1391, 1205, 1867, 1895, 1439, 1719, 1185, 1685, 1139, 1229, 1791, 1821, 1295, 0, 0, 0, 0, 
	1193, 1951, 1469, 1737, 1047, 1227, 1989, 1717, 1735, 1643, 1857, 1965, 1405, 1575, 1907, 1173, 1299, 0, 0, 0, 
	1641, 1887, 1129, 1357, 1543, 1279, 1687, 1975, 1839, 1775, 1109, 1337, 1081, 1435, 1603, 2037, 1249, 1153, 0, 0, 
	1999, 1065, 1387, 1977, 1555, 1915, 1219, 1469, 1889, 1933, 1819, 1315, 1319, 1693, 1143, 1361, 1815, 1109, 1631, 0, 
	1253, 1051, 1827, 1871, 1613, 1759, 2015, 1229, 1585, 1057, 1409, 1831, 1943, 1491, 1557, 1195, 1339, 1449, 1675, 1679, 
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	3475, 2685, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	3865, 2883, 2519, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	4019, 3383, 3029, 2397, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	2725, 3703, 3391, 2235, 2669, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	2489, 3151, 2695, 3353, 4029, 3867, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	2467, 2137, 3047, 3881, 3125, 2683, 3631, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	2739, 3163, 2137, 4031, 2967, 3413, 3749, 2301, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	3443, 2305, 3365, 2231, 2127, 3697, 3535, 4041, 2621, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	3641, 2777, 2789, 2357, 3003, 2729, 3229, 2925, 3443, 2291, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	3567, 2361, 2061, 2219, 3905, 2285, 2871, 3187, 2455, 2783, 2685, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
	4043, 2615, 2385, 3911, 3267, 2871, 3667, 3037, 2905, 2921, 2129, 2299, 0, 0, 0, 0, 0, 0, 0, 0, 
	2315, 2997, 3743, 2729, 3117, 2297, 2585, 3141, 3283, 3943, 3613, 3345, 4047, 0, 0, 0, 0, 0, 0, 0, 
	3967, 3069, 3377, 3909, 3691, 2439, 2533, 3075, 2129, 3319, 3433, 3035, 2745, 2631, 0, 0, 0, 0, 0, 0, 
	3023, 3349, 2111, 2385, 3907, 3959, 3425, 3801, 2135, 2671, 2637, 2977, 2999, 3107, 2277, 0, 0, 0, 0, 0, 
	2713, 2695, 3447, 2537, 2685, 3755, 3953, 3901, 3193, 3107, 2407, 3485, 2097, 3091, 2139, 2261, 0, 0, 0, 0, 
	3065, 4059, 2813, 3043, 2849, 3477, 3205, 3381, 2747, 3203, 3937, 3603, 3625, 3559, 3831, 2243, 2343, 0, 0, 0, 
	3999, 3183, 2717, 2307, 2103, 3353, 2761, 2541, 2375, 2327, 3277, 2607, 3867, 3037, 2163, 2261, 3649, 2929, 0, 0, 
	2543, 2415, 3867, 3709, 3161, 2369, 4087, 2205, 3785, 2515, 2133, 2913, 3941, 3371, 2605, 3269, 3385, 3025, 2323, 0, 
	2939, 2775, 3663, 2413, 2573, 2205, 3821, 3513, 2699, 3379, 2479, 2663, 2367, 2517, 3027, 3201, 3177, 3281, 4069, 2069, 
	};

	
	public int[] getMatrix (int width, int height)
	{
		int[] cols = new int[width];
		//cols = (u32*)malloc(width * sizeof(u32));

		if(width >= 2 && width <= 20 && height >= 7 && height <= 12) { // get it from the array
			//memcpy(cols, &mats[(height - 7) * 400 + (width - 1) * 20], width * sizeof(u32));
			System.arraycopy(mats, (height - 7) * 400 + (width - 1) * 20, cols, 0, width);
		}
        else { // generate a random one
			int i, j;
			int r=0, mask, bop;

			//boost::mt19937 generator( 1 );
			//boost::variate_generator< boost::mt19937&, boost::uniform_int< > > rng( generator, boost::uniform_int< >( 0, RAND_MAX ) );
            Random rn = new Random();


			mask = (1 << (height - 2)) - 1;
			bop = (1 << (height - 1)) + 1;
			if((1 << (height - 2)) < width) {
				// fprintf(stderr, "Cannot generate matrix for this payload. Choose a higher constraint height.\n");
				// generate the columns randomly but let first and last row be full of 1s.
				// I know, there will be identical columns.
				for(i = 0; i < width; i++)
                {
					r = ((rn.nextInt(32768) & mask) << 1) + bop;
					cols[i] = r;
				}
			}
            else {
				for(i = 0; i < width; i++)
                {
					for(j = -1; j < i;)
                    {
						r = ((rn.nextInt(32768) & mask) << 1) + bop;
						for(j = 0; j < i; j++) {
							if(cols[j] == r)
								break;
						}
					}
					cols[i] = r;
				}
			}

		}
		return cols;
		
	}
}
