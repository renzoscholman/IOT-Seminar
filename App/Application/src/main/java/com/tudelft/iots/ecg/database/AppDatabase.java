package com.tudelft.iots.ecg.database;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.content.Context;

import com.tudelft.iots.ecg.database.interfaces.ActivityDao;
import com.tudelft.iots.ecg.database.interfaces.ECGDao;
import com.tudelft.iots.ecg.database.interfaces.HeartRateDao;
import com.tudelft.iots.ecg.database.model.Activity;
import com.tudelft.iots.ecg.database.model.ECG;
import com.tudelft.iots.ecg.database.model.HeartRate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Database(entities = {HeartRate.class, ECG.class, Activity.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    public abstract HeartRateDao heartRateDao();
    public abstract ECGDao ecgDao();
    public abstract ActivityDao activityDao();

    protected long startTime = 0;

    private static AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "heart-rate-storage")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    public List<HeartRate> getDefaultHeartRates(){
        List<Double> hrs = Arrays.asList(92.3076923076923, 92.3076923076923, 95.23809523809524, 92.3076923076923, 90.9090909090909, 92.3076923076923, 90.9090909090909, 92.3076923076923, 92.3076923076923, 92.3076923076923, 92.3076923076923, 93.75, 92.3076923076923, 95.23809523809524, 93.75, 92.3076923076923, 96.7741935483871, 95.23809523809524, 96.7741935483871, 98.36065573770492, 96.7741935483871, 93.75, 98.36065573770492, 93.75, 96.7741935483871, 96.7741935483871, 95.23809523809524, 98.36065573770492, 98.36065573770492, 100.0, 98.36065573770492, 100.0, 100.0, 100.0, 103.44827586206897, 100.0, 101.69491525423729, 86.95652173913044, 133.33333333333334, 105.26315789473685, 107.14285714285714, 103.44827586206897, 103.44827586206897, 105.26315789473685, 103.44827586206897, 101.69491525423729, 101.69491525423729, 100.0, 98.36065573770492, 101.69491525423729, 100.0, 101.69491525423729, 100.0, 100.0, 100.0, 100.0, 101.69491525423729, 98.36065573770492, 100.0, 101.69491525423729, 101.69491525423729, 90.9090909090909, 120.0, 103.44827586206897, 103.44827586206897, 101.69491525423729, 103.44827586206897, 101.69491525423729, 101.69491525423729, 100.0, 101.69491525423729, 103.44827586206897, 100.0, 101.69491525423729, 103.44827586206897, 101.69491525423729, 103.44827586206897, 105.26315789473685, 103.44827586206897, 103.44827586206897, 105.26315789473685, 105.26315789473685, 105.26315789473685, 103.44827586206897, 105.26315789473685, 103.44827586206897, 101.69491525423729, 103.44827586206897, 101.69491525423729, 100.0, 103.44827586206897, 103.44827586206897, 100.0, 103.44827586206897, 105.26315789473685, 103.44827586206897, 103.44827586206897, 105.26315789473685);
        List<Integer> timestamps = Arrays.asList(65, 131, 195, 261, 328, 394, 461, 527, 593, 659, 725, 790, 856, 920, 985, 1051, 1114, 1178, 1241, 1303, 1366, 1431, 1493, 1558, 1621, 1684, 1748, 1810, 1872, 1933, 1995, 2056, 2117, 2178, 2237, 2298, 2358, 2428, 2474, 2532, 2589, 2648, 2707, 2765, 2824, 2884, 2944, 3005, 3067, 3127, 3188, 3248, 3309, 3370, 3431, 3492, 3552, 3614, 3675, 3735, 3795, 3862, 3913, 3972, 4031, 4091, 4150, 4210, 4270, 4331, 4391, 4450, 4511, 4571, 4630, 4690, 4749, 4807, 4866, 4925, 4983, 5041, 5099, 5158, 5216, 5275, 5335, 5394, 5454, 5515, 5574, 5633, 5694, 5753, 5811, 5870, 5929, 5987);
        ArrayList<HeartRate> heartRates = new ArrayList<>();
        for(int i = 0; i < hrs.size(); i++){
            HeartRate hr = new HeartRate();
            hr.heartRate = (int) Math.round(hrs.get(i));
            hr.timestamp = startTime + timestamps.get(i) * 10;
            heartRates.add(hr);
        }
        return heartRates;
    }

    protected List<Integer> getMoreECGData1(){
        return Arrays.asList(2870,2931,2992,3103,2820,2720,2551,2585,2745,2867,2931,3102,3063,2961,2753,2688,2778,2813,3177,3055,2753,2767,2625,2674,2778,2634,2621,2637,2593,2965,4095,4095,3676,2687,2659,2624,2591,2621,2559,2437,2425,2341,2379,2439,2445,2480,2512,2515,2541,2512,2579,2675,2717,2853,2892,2941,2813,2878,2800,2722,2733,2944,3129,3139,3088,3095,3120,3125,3025,3026,2939,3013,2833,2814,2804,2880,3018,3099,3114,3216,3105,3120,3063,2811,2855,2767,2717,2599,2758,2672,3005,4095,4095,4095,3376,3159,3138,3148,2967,2800,2929,2884,2896,2787,2741,2757,2767,2877,2897,2862,3021,3057,2839,2801,2800,2864,2638,2704,2611,2448,2377,2391,2454,2587,2619,2663,2609,2603,2657,2595,2627,2541,2416,2403,2386,2490,2771,2786,2782,2813,2768,2612,2559,2334,1743,1857,2083,2202,2463,2469,2559,3011,4095,4095,3581,2966,2812,2736,2767,2833,2713,2692,2621,2741,2862,2944,3043,2997,3063,2986,2960,2955,2954,2835,2928,2867,3054,2986,2927,2846,2816,2832,2757,2727,2752,2734,2644,2709,2816,2759,2768,2831,2736,2773,2736,2702,2826,2849,2854,2864,2924,2950,2988,2848,2837,2802,2693,2768,2800,2779,2768,2841,3569,4095,4095,2943,2763,2892,2791,2748,2867,2913,2787,2944,2992,2998,2994,2859,2943,2978,2915,2781,2726,2729,2705,2787,2591,2602,2736,2611,2586,2601,2559,2626,2802,2723,2682,2602,2641,2587,2551,2427,2464,2494,2419,2559,2547,2427,2642,2674,2759,2815,2823,2798,2805,2674,2816,2783,2831,2851,2819,2759,2882,3325,4095,4095,3374,2707,2911,2739,2800,2859,2842,2839,2970,2887,2857,2780,2858,2865,2821,2852,2885,2752,2791,2779,2909,2949,2834,2686,2713,2641,2665,2624,2706,2719,2771,2704,2810,2730,2688,2626,2693,2858,2973,3015,3071,3043,3268,3086,2839,2448,2541,2611,2713,2843,3217,3053,2795,2619,2671,2591,2697,2818,3081,3984,4095,3703,2632,2448,2523,2486,2559,2624,2559,2559,2530,2495,2559,2531,2512,2453,2558,2512,2539,2505,2403,2321,2263,2204,2199,2225,2310,2363,2333,2423,2336,2559,2819,2753,2686,2640,2879,3071,2768,2525,2437,2477,2843,3154,3152,3122,3193,3022,3119,3120,2935,2658,2658,2685,2717,2689,2654,2832,3439,4095,4095,3920,3081,2841,2805,2597,2597,2557,2613,2749,2695,2643,2621,2610,2651,2625,2739,2639,2608,2662,2805,2947,3119,3312,3049,2897,2862,2812,2768,2582,2653,2755,2841,3019,3222,3120,3014,2923,2783,2598,2475,2527,2689,2949,2809,2825,2939,2829,2765,2694,2722,2626,2618,2610,2702,2724,2752,2781,3505,4095,4095,3184,2859,2646,2608,2486,2645,2547,2559,2559,2714,2803,2813,2734,2875,2957,3051,2879,2847,2775,2625,2715,2831,2958,3149,3157,3027,2875,2985,2949,2848,2870,2898,2903,2957,2880,3002,2962,2882,3030,2935,2881,2880,2960,3089,3007,3107,2971,2944,2807,2667,2512,2531,2599,2736,2711,2687,2783,3857,4095,3414,1441,1422,1629,1623,1990,2591,2724,2795,2355,2327,2435,2594,2714,2800,2690,2558,2474,2487,2559,2350,2368,2929,2975,3023,2795,2814,2665,2765,2768,2896,2955,2975,2858,2835,2777,2751,2624,2774,2818,2853,2690,2610,2520,2615,2559,2517,2559,2643,2645,2601,2626,2631,2590,2670,2781,2679,2966,4019,4095,3406,2385,2220,2278,2538,2807,2859,2861,2911,2899,2816,2739,2516,2463,2425,2677,2725,2711,2523,2429,2349,2352,2439,2495,2529,2640,2613,2559,2551,2640,2591,2651,2690,2846,2983,3024,3113,2986,2782,2752,2685,2896,2926,3140,3214,3244,3230,3071,2736,2694,2782,2941,2978,3059,3309,3141,3421,4095,4095,4095,2857,2682,2815,2581,2524,2675,2736,2780,2751,2757,2763,2790,2735,2729,2665,2645,2699,2899,2864,2903,2989,2945,2797,2702,2551,2461,2498,2401,2442,2526,2543,2723,2605,2431,2556,2713,2855,3051,2951,2895,2843,2943,3028,3030,3129,3242,3205,3219,2993,2800,2673,2785,2917,3039,3101,3385,4095,4095,4095,3191,3049,2863,3009,2952,2859,2893,2976,3090,2884,3058,2964,2907,2865,2707,2673,2690,2686,2720,2601,2624,2725,2671,2758,2832,2768,2656,2608,2522,2559,2508,2768,2864,2911,2809,2763,2681,2960,3047,2870,2839,2785,2798,2814,2851,3082,2973,3135,2859,2688,2556,2641,2555,2522,2559,2641,2886,3639,4095,3413,2587,2342,2384,2467,2415,2393,2327,2323,2276,2234,2331,2398,2439,2459,2507,2626,2630,2659,2544,2502,2439,2320,2385,2325,2311,2503,2594,2640,2691,2862,2929,2946,3010,3089,2943,2966,2855,2863,2843,2889,2864,3024,3109,3173,3071,3015,2923,2915,2873,2803,2759,2706,2702,2815,2775,2847,3888,4095,4095,3013,2885,3155,3098,3063,2880,2673,2661,2486,2299,2369,2454,2709,2811,2770,2928,3037,2987,2717,2717,2798,2714,2758,2674,2692,2749,2646,2705,2869,2817,2773,3025,2880,2781,2650,2613,2539,2350,2278,2159,2263,2352,2405,2529,2739,2929,2975,2979,2847,2704,2654,2766,2774,2676,2679,2631,3152,4095,4095,3415,2896,2800,2859,2838,2850,2910,2979,2932,2863,2915,2947,2885,3009,3008,2962,2937,2999,2896,2838,2823,2975,2997,3024,2983,3041,2959,2961,2919,2839,2906,2768,2763,2847,2814,2782,2781,2929,2951,3010,2943,2951,3216,3486,3339,3181,3148,3328,3216,3125,3128,2990,2895,2967,2855,2949,2941,3122,3973,4095,4095,2957,2695,2640,2592,2559,2596,2613,2593,2609,2607,2608,2597,2653,2654,2688,2704,2674,2589,2583,2547,2483,2449,2398,2423,2387,2365,2375,2432,2490,2447,2471,2520,2557,2546,2559,2614,2615,2671,2447,2448,2415,2423,2481,2622,2786,2940,3280,3230,2971,2957,2963,2951,3003,2960,2928,2786,2845,3091,4095,4095,4095,2909,2752,2689,2582,2528,2473,2519,2480,2416,2507,2479,2485,2515,2672,2650,2606,2542,2601,2635,2678,2859,2946,2943,2853,2797,2812,2640,2641,2455,2599,2545,2547,2716,2784,2763,2733,2559,2559,2669,2538,2601,2559,2650,2733,2771,2738,2685,2650,2647,2691,2513,2581,2673,2657,2640,2785,3291,4095,4071,2897,2384,2320,2170,2077,2074,2144,2239,2415,2338,2347,2394,2448,2514,2469,2329,2448,2429,2535,2423,2451,2501,2783,2772,2843,2799,2721,2703,2835,2814,2850,2800,2778,2736,2759,2802,2787,2829,2932,2947,2959,2906,3036,2987,2906,2833,2966,2838,2785,2608,2720,2698,2723,2559,2768,2819,3141,4007,4095,4095,3037,2930,2737,2764,2973,3008,3216,3141,2963,2954,2960,2944,2944,2970,3021,2998,3026,2928,3057,3065,3216,3275,3065,2897,2759,2743,2863,2843,2928,2913,2835,2778,2789,2899,2863,2833,2891,2832,2850,2806,2790,2631,2725,2736,2768,2736,2832,2752,2656,2507,2426,2353,2467,2578,2621,2548,2663,3093,4095,4095,3521,2595,2472,2359,2427,2551,2797,2895,3023,3201,3294,3071,2893,2704,2624,2512,2544,2722,2839,2867,2794,2851,2863,2833,2707,2639,2528,2544,2416,2459,2459,2422,2352,2448,2582,2587,2512,2467,2522,2625,2737,2806,3067,3332,3117,3215,3175,3111,2817,2590,2474,2538,2693,2752,2726,2624,2533,2883,4071,4095,3014,2455,2432,2414,2368,2352,2448,2495,2483,2461,2425,2541,2485,2502,2511,2340,2483,2530,2529,2624,2667,2590,2509,2476,2443,2286,2339,2292,2402,2425,2498,2496,2371,2465,2539,2704,2786,2755,2825,2957,2858,2771,2704,2733,3126,3408,3361,3301,3412,3180,3024,3006,2919,2811,2870,2803,3003,3775,4095,4095,3171,2896,2909,2878,2778,2814,2861,2914,2816,2811,2690,2732,2774,2715,2807,2915,2821,2957,3167,3038,3156,3339,3142,3053,2718,2736,2902,3051,2991,2973,2915,3047,3315,3227,3168,2994,2896,2898,3117,3221,3263,3348,3406,3127,3141,3239,2997,2992,2946,2893,2936,2874,2790,2833,2775,2721,3014,4095,4095,3342,2598,2518,2446,2293,2378,2409,2359,2417,2411,2582,2595,2559,2659,3009,3174,2960,2820,2782,2743,2493,2529,2595,2619,2704,2946,2963,3029,3069,2992,2987,3018,3031,2903,2821,2747,2628,2559,2627,2681,2706,2775,2770,2768,2780,2749,2853,2622,2599,2532,2498,2523,2557,2503,2633,2475,3025,4095,4095,3274,2771,2906,2803,2917,3182,3331,3277,3049,2769,2751,2621,2592,2539,2673,2746,2946,2983,3088,2944,2736,2768,2559,2660,2667,2738,2699,2532,2383,2463,2499,2559,2559,2551,2502,2686,2643,2559,2585,2633,2671,2691,2607,2734,2832,2559,2358,2281,2087,1999,2165,2285,2271,2359,2443,2601,3422,4095,3427,2640,2542,2581,2591,2612,2718,2826,2811,2699,2662,2798,2983,3088,3119,3056,2868,2836,2850,2926,2848,2676,2595,2742,2757,2852,2934,2866,2867,2928,2897,2974,2948,2951,2918,2855,2813,2726,2655,2651,2910,2965,3043,3042,3172,3185,3234,3271,3337,3026,2970,2817,2891,2913,2723,2683,3280,4095,4095,3825,3087,2993,2852,2854,2723,2675,2740,2608,2592,2611,2545,2543,2559,2668,2762,2779,2795,2823,2800,2749,2630,2706,2716,2768,2641,2634,2639,2679,2734,2851,2811,2647,2640,2559,2549,2593,2477,2490,2519,2416,2656,2797,3043,3270,3215,3119,3151,3108,3007,2925,3019,3023,2986,2777,2633,3088,4095,4095,3221,2874,2802,2800,2675,2727,2879,3093,3035,2861,2843,2841,2781,2714,2704,2778,2709,2685,2790,2776,2800,2815,2765,2815,2832,2771,2771,2735,2733,2737,2740,2810,2709,2686,2777,2810,2811,2875,2859,2750,2782,2918,2945,3047,3171,3155,3059,2926,2601,2559,2646,2602,2515,2614,2745,3485,4095,4095,3365,2999,2865,2751,2697,2757,2790,2751,2672,2667,2587,2416,2534,2495,2512,2707,2709,2694,2663,2655,2687,2651,2637,2725,2644,2640,2598,2581,2480,2480,2429,2405,2479,2445,2415,2441,2415,2418,2441,2475,2512,2608,2581,2589,2706,2736,2752,2812,2742,2727,2529,2486,2381,2662,2801,3529,4095,4095,3626,2943,2791,2783,2590,2591,2618,2645,2612,2687,2657,2647,2754,2896,2878,2873,2797,2752,2687,2670,2544,2583,2512,2598,2586,2738,2742,2861,2959,2897,2865,2769,2675,2893,3029,3067,2983,2731,2866,2877,2707,2833,2721,2832,2903,2981,3098,3193,2922,2784,2834,2791,2959,3023,2885,3071,3809,4095,4095,3082,2559,2559,2494,2477,2446,2484,2449,2365,2320,2597,2618,2631,2737,2707,2710,2619,2499,2640,2422,2490,2555,2618,2768,2785,2830,2773,2834,2926,2799,2685,2446,2517,2663,2649,2720,2705,2915,3328,3262,3135,3105,3088,3169,3120,3034,3081,2832,2640,2352,2356,2470,2522,2715,2771,3088,4095,4095,3845,2814,2513,2443,2367,2352,2331,2397,2320,2303,2373,2290,2559,2559,2579,2704,2659,2640,2704,2800,2930,3090,2925,2907,2816,2761,2729,2509,2454,2530,2623,2747,2942,2954,2800,2534,2550,2500,2539,2540,2659,2749,2811,3120,3115,2967,2995,2914,2865,2635,2559,2581,2629,2559,2615,3137,4095,4095,3180,2491,2583,2365,2194,2249,2289,2322,2213,2321,2496,2487,2555,2617,2479,2414,2645,2762,2803,2747,2894,2926,2751,2625,2558,2559,2805,2989,3311,3366,3383,3154,3043,2822,2915,2918,2863,2798,2787,2785,2917,2992,3139,3019,3070,3095,3030,2899,2849,2857,2927,2725,2697,2645,2559,2670,3015,3914,4095,4095,3151,2769,2817,2768,2946,2911,2974,2971,3184,2964,2836,2955,2969,2781,2681,2679,2730,2815,2916,2928,2953,2928,2914,2849,2790,2688,2706,2686,2606,2385,2495,2487,2631,2599,2621,2608,2739,2641,2639,2599,2672,2770,2879,2823,2832,3025,2919,2868,2736,2515,2416,2481,2439,2485,2559,2626,2768,3745,4095,4095,3215,2919,2641,2705,2806,2851,2820,2598,2448,2519,2738,3195,3568,3559,3421,3466,3190,2964,2971,2960,2885,2783,2729,2598,2591,2672,2624,2779,2778,2832,2751,2676,2723,2736,2621,2736,2660,2676,2622,2690,2749,2765,2935,3013,2970,2927,2768,2701,2634,2559,2858,3089,3009,2829,2843,3095,3655,4095,4095,3025,2624,2691,2733,2709,2832,2738,2703,2645,2559,2700,2535,2544,2537,2468,2503,2487,2617,2559,2661,2559,2558,2695,2398,2530,2706,2682,2662,2765,2903,2830,2621,2481,2398,2399,2301,2246,2325,2359,2451,2436,2403,2439,2679,2974,3181,3178,2912,2813,2666,2711,2878,2987,3093,3093,3070,3322,4095,4095,3790,2791,2755,2752,2791,2705,2531,2652,2672,2877,2945,2999,3102,3162,3185,3156,3121,3151,3162,3162,3110,3044,2943,2776,2720,2659,2645,2704,2765,2755,2743,2863,2979,2954,2833,2763,2821,2910,2960,2975,3082,2962,2934,2966,3008,3135,3262,3119,2925,2765,2691,2531,2544,2622,2703,2514,2546,2449,2874,4063,4095,3310,2640,2613,2502,2478,2497,2431,2409,2446,2509,2403,2631,2602,2485,2256,2279,2283,2155,2144,2163,2298,2325,2384,2290,2268,2251,2239,2334,2463,2537,2622,2658,2502,2316,2305,2207,2227,2425,2736,2992,3154,3219,3067,2756,2685,2875,2809,2709,2784,2704,2857,2859,2754,2854,2720,2727,3095,4095,4095,3549,2858,2706,2604,2587,2607,2629,2629,2709,2838,3001,3227,3302,3150,2901,2510,2310,2432,2467,2635,2833,2943,3042,3151,3118,3036,2945,2861,2864,2928,2850,2807,2823,2799,2832,2867,2783,2864,2867,2814,2871,2971,2979,2949,3055,2981,2741,2495,2462,2367,2352,2450,2559,2708,2800,2731,2970,3815,4095,4095,3350,3106,3019,2874,2844,2690,2615,2639,2688,2738,2979,2950,3035,3127,3123,3054,3065,3111,3046,3069,2964,3084,3181,3119,3173,3027,2969,2994,2943,2990,2976,2949,2925,2899,2883,2881,2922,2991,3071,3031,2898,2829,2967,2967,2947,2979,3023,3002,2979,2901,2871,2794,2765,2742,2800,2875,2947,3495,4095,4095,3344,2954,2917,2960,2960,2866,2725,2757,2755,2735,2623,2559,2619,2533,2583,2534,2541,2592,2553,2545,2507,2542,2464,2549,2647,2502,2447,2421,2425,2334,2288,2375,2293,2287,2353,2479,2559,2603,2503,2583,2612,2679,2739,2805,2795,2609,2559,2622,2541,2518,2559,2551,2631,2674,2720,2919,3525,4095,4095,3131,2609,2538,2527,2618,2543,2531,2621,2548,2635,2615,2517,2473,2511,2501,2415,2526,2544,2519,2496,2559,2544,2491,2530,2429,2597,2800,2925,3157,3261,3134,3056,2879,2475,2261,2272,2318,2467,2615,2785,2875,2867,2978,2993,2913,2822,2779,2750,2777,2832,2926,2878,2960,2986,2815,3103,4095,4095,3824,2784,2723,2647,2727,2711,2608,2650,2724,2699,2639,2609,2455,2496,2401,2470,2460,2583,2539,2619,2559,2476,2624,2777,2821,2802,2768,2640,2769,2800,2830,2662,2559,2707,2726,3024,3264,3213,3167,3344,3195,3195,3069,2911,2911,2937,2847,2896,2909,2906,2881,3054,3056,3122,3065,2934,3055,3950,4095,4095,3043,2797,2625,2581,2518,2525,2559,2644,2613,2714,2810,2831,2799,2866,2960,3022,2985,3067,3023,2847,2771,2885,2797,2832,2719,2762,2761,2757,2736,2770,2710,2677,2663,2732,2709,2839,2727,2649,2602,2698,2559,2598,2589,2704,2747,2749,2669,2630,2454,2553,2530,2598,2437,2502,2447,2733,3696,4095,3175,2430,2022,2265,2119,2165,2173,2274,2476,2494,2350,2434,2402,2439,2522,2646,2770,2785,2711,2731,2657,2597,2489,2640,2849,2953,3033,3088,3081,3022,2957,2878,2802,2555,2559,2642,2594,2679,2730,2690,2697,2822,2834,2804,2448,2552,2713,2641,2631,2687,2706,2736,2642,2539,2541,3015,4095,4095,3424,2741,2880,2762,2773,2843);
    }

    public List<ECG> getDefaultECGValues(){
        List<Integer> ecg_vals = new ArrayList<>(Arrays.asList(4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,3891,3863,3921,3639,3421,3223,3119,2979,2810,2623,2397,2231,2007,1923,1830,1713,1600,1504,1591,1509,1450,1309,1182,1152,1078,1072,956,1102,914,816,772,762,704,704,720,552,529,548,432,320,341,234,144,176,228,336,621,592,692,968,1755,2271,1730,821,740,738,786,817,868,881,880,976,1076,964,916,962,1040,960,1070,1152,1242,1318,1408,1428,1378,1488,1491,1428,1437,1487,1354,1270,1362,1362,1439,1461,1520,1535,1600,1531,1687,1716,1869,1829,1824,1863,1934,1919,2069,2095,2250,2322,2551,2531,2531,2343,2202,1990,1969,1993,2223,2342,2454,2407,2335,2655,3583,3615,2832,2327,2306,2286,2210,2260,2191,2259,2263,2335,2418,2302,2362,2429,2403,2251,2319,2319,2422,2399,2492,2433,2370,2319,2342,2303,2391,2367,2385,2397,2463,2431,2351,2480,2525,2384,2290,2391,2667,2705,2587,2449,2250,2303,2378,2442,2559,2639,2823,3295,3327,3323,3247,3103,2947,2879,2886,2887,2943,2941,3071,3607,4095,4095,4083,3183,3063,2974,2906,2893,2919,2815,2967,2911,2883,2640,2438,2443,2513,2727,2887,3023,3071,3071,3253,3259,3263,3059,3071,3135,3269,3259,3367,3303,3247,2991,3043,2883,2887,2879,3055,3119,2875,3039,3143,3033,3043,3039,3099,3139,3248,3071,3093,3071,3031,3107,3039,3007,2834,2901,2899,3056,2837,2869,2787,2739,2743,3015,3751,4095,4095,3223,2743,2589,2625,2434,2451,2527,2395,2423,2499,2453,2559,2495,2670,2793,2839,2931,2935,2787,2512,2559,2482,2495,2512,2531,2719,2767,2769,2987,3019,2879,2810,2695,2655,2686,2619,2711,2835,2695,2759,2482,2295,2303,2242,2331,2513,2334,2187,2345,2258,2303,2659,2703,2544,2557,2670,2615,2559,2535,2519,2763,2843,2864,3327,4095,4095,3755,2879,2805,2559,2415,2327,2623,2615,2631,2759,2653,2557,2710,2479,2544,2775,2997,2847,2894,2923,2791,2639,2641,2559,2533,2447,2401,2367,2322,2323,2423,2495,2507,2480,2539,2653,2479,2507,2485,2496,2493,2639,2515,2449,2355,2375,2277,2449,2667,2826,2975,3109,3067,2957,3047,3199,3151,2975,2767,2762,2699,2559,2747,3378,4095,4095,4051,3231,3143,2879,2851,2833,2769,2943,2910,2879,2950,3056,3023,2999,2943,2895,2942,2950,2975,3031,3135,3051,3021,3033,2987,2903,2871,2834,2835,2919,2863,2739,2830,2823,2879,2807,2832,2663,2703,2757,2834,2911,2731,2773,2975,2815,2402,2399,2343,2474,2559,2671,2655,2719,2896,3089,2661,2421,2523,2559,2701,2710,2768,2955,3247,4095,4095,4095,2897,2814,2731,2737,2559,2531,2619,2635,2487,2512,2687,2670,2623,2559,2643,2489,2559,2527,2495,2589,2651,2531,2543,2454,2385,2418,2483,2434,2480,2595,2535,2512,2551,2691,2559,2594,2559,2493,2475,2541,2667,2685,2815,2766,2751,2751,2691,2647,2727,2763,2807,2855,2773,2786,2876,2733,2686,2631,2619,2583,2651,2687,2767,3570,4095,4095,3069,2651,2586,2557,2543,2687,2821,2608,2823,2927,2911,2941,3223,2979,3047,3011,2927,2865,2757,2778,2800,2871,2885,2865,2867,2875,2835,2719,2803,2749,2736,2747,2623,2597,2627,2559,2543,2615,2611,2559,2443,2407,2128,2247,2176,2255,2269,2431,2513,2385,2399,2427,2295,2205,2291,2165,2119,2159,2263,2198,2257,2307,2835,4007,4095,2893,2469,2279,2256,2229,2287,2160,2183,2403,2830,2911,3003,2967,3071,2991,3003,2839,2941,2979,3039,3099,3159,3095,3007,3025,2943,2987,2975,2917,2879,2995,2879,3115,3335,3007,2901,2975,2903,2800,2799,2815,2833,2750,2877,2787,2723,2833,3051,2995,2959,3163,3027,2951,2829,2769,2719,2559,2387,2653,2717,2911,3199,3495,4095,4095,4095,3887,3119,3125,2959,2928,2893,2935,2931,2925,2891,2893,2931,2896,2803,2799,2725,2693,2595,2699,2959,3023,3011,2960,2791,2775,2739,2619,2559,2535,2559,2643,2559,2523,2512,2625,2640,2559,2519,2650,2559,2559,2595,2643,2669,2558,2501,2591,2623,2621,2591,2583,2319,2365,2257,2343,2521,2623,2611,2751,2833,2941,2895,2831,3203,4095,4095,3831,2883,2815,2639,2597,2591,2687,2719,2815,2771,2755,2704,2755,2787,2755,2819,2869,2847,2736,2797,2813,2727,2791,2739,2687,2609,2549,2543,2467,2609,2671,2605,2738,2795,2837,2690,2739,2767,2679,2687,2847,2883,2934,2871,2747,2832,2967,2960,2879,2847,2802,2773,2813,2887,2929,2895,2833,2846,2919,2695,2691,2532,3167,4095,4095,3567,2703,2641,2695,2607,2599,2559,2555,2609,2555,2589,2595,2655,2687,2605,2737,2769,2559,2657,2715,2667,2607,2489,2407,2375,2501,2247,2327,2405,2419,2263,2090,2315,2555,2759,2815,2775,2791,2723,2705,2559,2501,2555,2695,2794,2838,2935,2982,2901,2879,2895,2841,2747,2717,2703,2705,2765,2545,2559,2651,2669,2677,2695,2993,4055,4095,4095,3007,2783,2719,2799,2715,2731,2715,2559,2545,2627,2635,2674,2871,2845,2727,2743,2927,2883,3007,2843,2831,2767,2783,2687,2626,2613,2605,2683,2703,2859,2851,2869,2997,2938,2893,2762,2683,2813,2759,2879,2941,2987,3066,2958,2807,2823,2855,2811,2862,2927,2879,2896,2795,2677,2675,2687,2559,2559,2591,2666,2843,3607,4095,4095,3099,2607,2606,2467,2384,2325,2385,2391,2448,2493,2423,2479,2637,2723,2677,2731,2815,2939,2963,2847,2715,2719,2705,2674,2871,2897,2879,2826,2896,2815,2864,2895,2815,2733,2735,2693,2767,2693,2803,2703,2855,2863,2864,2762,2635,2651,2768,2723,2743,2787,2799,2751,2641,2657,2635,2647,2673,2775,2175,2395,2614,2791,3492,4095,4095,3153,2830,2680,2613,2641,2647,2096,1683,1665,1910,2133,2330,2384,2363,2174,2299,2389,2527,2624,2669,2630,2491,2529,2607,2594,2583,2644,2704,2723,2775,2742,2653,2787,2775,2787,2811,2653,2750,2806,2594,2556,2559,2512,2549,2512,2580,2688,2773,2767,2896,3008,3040,3120,3306,3117,3011,2975,3010,2944,3024,2919,2896,3619,4095,4095,3989,3215,3151,2951,2795,2797,2926,2937,2929,2874,2882,2827,2871,2871,2955,2988,3027,3088,3024,2983,2929,2925,2739,2622,2685,2645,2541,2637,2654,2612,2594,2675,2559,2519,2538,2624,2691,2734,2707,2736,2800,2878,2990,2925,2770,2635,2556,2635,2891,3203,3202,3262,3007,2859,2763,2559,2542,2555,2419,2503,3055,4095,4095,4095,3244,3061,2831,2757,2650,2665,2559,2633,2752,2746,2774,2869,2798,2614,2827,2761,2738,2559,2448,2463,2448,2422,2463,2527,2437,2496,2497,2402,2402,2426,2500,2559,2547,2707,2864,2894,2844,2791,2743,2915,3011,3247,3487,3443,3435,3405,3243,3197,3071,2922,2870,2919,2786,2896,2962,2887,3060,3169,3235,3188,3888,4095,4095,3195,2800,2819,2739,2789,2773,2742,2756,2819,2901,2886,2895,2896,2935,3049,3023,3066,3085,3122,3250,3249,3330,3245,3115,2799,2432,2317,2503,2683,2688,2780,2703,2499,2439,2227,1945,1994,2019,1934,2224,2451,2887,2886,2706,2546,2682,2591,2559,2547,2458,2358,2349,2347,2434,2301,2342,2345,2361,2290,2446,3024,4011,3435,2465,2258,2323,2306,2347,2287,2226,2287,2317,2374,2559,2559,2701,2816,2957,2929,2838,2777,2807,3055,3257,3389,3344,3165,3087,2961,2826,2839,2698,2767,2721,2735,2670,2715,2789,2814,2934,2960,2866,2893,2918,2816,2727,2704,2742,2784,2819,2891,2992,2855,2941,2950,2878,2919,2966,2842,2767,2695,2794,3613,4095,4095,3159,2863,2807,2699,2756,2875,3167,3186,3117,3157,2960,2939,3086,3109,3123,2946,2966,2928,2865,2868,3009,3007,3008,2998,3014,2938,2817,2736,2673,2702,2660,2709,2678,2693,2640,2630,2615,2582,2666,2694,2635,2617,2679,2651,2702,2688,2739,2787,2739,2559,2544,2629,2745,2767,2686,2787,2711,2758,2864,2909,3657,4095,4095,3714,3088,3029,2899,2896,2974,2932,2985,2995,3029,3120,3218,3105,3119,2960,3010,3045,2935,2880,2887,2891,2864,2839,2807,2747,2686,2691,2725,2702,2703,2677,2735,2698,2746,2726,2725,2715,2782,2727,2727,2689,2738,2690,2682,2725,2811,2737,2845,2864,2863,2778,2727,2701,2704,2615,2633,2480,2630,2714,2695,2609,2807,3527,4095,4095,3169,2800,2639,2607,2465,2587,2768,2762,2778,2643,2672,2525,2667,2863,3026,3057,2939,2779,2734,2657,2672,2622,2688,2721,2553,2491,2444,2475,2586,2643,2640,2559,2531,2594,2553,2593,2629,2674,2542,2480,2442,2466,2523,2553,2544,2602,2663,2681,2694,2730,2819,2805,2679,2899,3035,2942,2703,2559,2365,2723,4095,4095,4095,3727,3339,3091,2891,2847,2807,2718,2684,2697,2559,2523,2690,2745,2805,2883,2854,2924,2971,2917,2925,2882,2896,2768,2635,2613,2637,2689,2752,2799,2767,2831,2699,2768,2817,2814,2746,2623,2501,2638,2675,2768,2899,3010,2960,2910,2973,3134,3120,3093,3149,3066,2979,2874,2748,2656,2677,2475,2528,2437,2544,2629,3152,4095,4095,3760,2861,2795,2679,2654,2751,2750,2783,2839,2821,2815,2681,2770,2865,2768,2698,2481,2559,2559,2453,2395,2491,2519,2466,2486,2515,2559,2688,2709,2539,2559,2454,2559,2655,2651,2706,2795,2842,2988,3187,3202,3109,2996,2899,2831,2862,2800,2761,2768,2757,2659,2709,2732,2666,2637,2641,2614,2723,2626,2582,2704,3461,4095,3738,2620,2415,2359,2278,2331,2478,2539,2587,2534,2523,2431,2613,2698,2685,2670,2630,2630,2558,2431,2546,2806,2849,2606,2539,2476,2675,2675,2782,2750,2705,2545,2635,2668,2781,2701,2800,2699,2896,2833,2837,2832,2786,2749,2705,2645,2835,2883,2949,2861,2940,2885,2677,2681,2523,2484,2601,2467,2480,2477,2809,3723,4095,3375,2598,2605,2647,2624,2707,2752,2834,2976,3124,2955,2891,2974,2849,2798,2819,2933,3122,3408,3542,3398,3271,3158,3209,3210,3168,3065,2986,2973,2921,2979,2974,2989,2949,2987,2989,3041,2883,2934,2975,3015,2779,2847,2787,2709,2891,2974,2919,3007,3056,3095,2865,2901,2943,2905,2935,2845,2697,2781,2807,2770,3403,4095,3818,3056,3013,3067,2855,2960,2980,2897,2778,2809,2866,2768,2890,2863,2850,2919,2915,2941,2955,3092,3029,2899,2805,2743,2735,2613,2636,2655,2621,2639,2679,2681,2547,2557,2582,2527,2465,2465,2459,2418,2414,2389,2231,2341,2311,2387,2542,2640,2810,2865,2835,2916,2733,2650,2790,2770,2599,2489,2394,2242,2499,3792,4095,4095,3481,3009,2917,2770,2769,2754,2741,2591,2613,2526,2631,2720,2723,2736,2704,2704,2641,2619,2559,2502,2498,2481,2475,2355,2430,2451,2370,2263,2367,2526,2559,2522,2485,2559,2511,2633,2771,2793,2899,2998,2838,2609,2559,2506,2659,2659,3025,3021,2911,2830,2543,2698,2715,2914,2991,2982,2845,3143,3385,4095,4095,3610,2767,2743,2709,2625,2750,2788,2749,2673,2603,2557,2486,2635,2650,2545,2621,2632,2604,2459,2388,2326,2210,2479,2526,2768,2727,2496,2608,2543,2602,2846,3099,2837,2789,2750,2755,2917,2896,2974,3065,2810,2795,2845,2991,3123,3127,3127,2987,2839,2789,2802,2864,2868,2893,2885,2829,2939,2926,3182,4095,4095,4095,3365,2950,2929,2754,2717,2706,2579,2679,2704,2735,2826,3007,2753,2975,2983,2808,2731,2478,2347,2197,2214,2546,2896,3024,2927,3071,3071,3079,3085,2950,2811,2534,2496,2623,2806,2891,2811,2891,2735,2729,2808,2755,2799,2864,2842,2506,2789,2886,2688,2714,2650,2579,2480,2501,2517,2557,2524,2544,2559,3303,4095,3446,2506,2309,2450,2443,2422,2375,2519,2559,2559,2651,2736,2794,2869,2982,3018,2994,2992,2992,2953,2960,2947,2946,2909,2800,2855,2839,2769,2825,2702,2761,2890,2817,2790,2800,2725,2771,2599,2698,2539,2603,2522,2637,2763,2751,2887,3081,3231,3061,3151,3054,2768,2554,2476,2487,2506,2299,2297,2678,4095,4095,4060,3093,2933,2861,2850,2701,2688,2739,2723,2813,2943,2837,2938,2861,2877,2788,2841,2778,2850,2791,2751,2752,2701,2790,2923,2767,2773,2925,2960,3167,3256,2832,2617,2382,2416,2525,2771,3152,3323,3268,3254,3315,3145,3158,3087,3179,3238,3056,3050,3028,2937,3086,3245,3263,3135,3139,3088,3312,3952,4095,4095,3747,3037,2925,2989,2960,2973,2983,2858,2897,3035,2890,2953,3037,3028,3005,2958,2951,2841,2853,2820,2544,2717,3118,3303,3293,3235,3143,3107,2862,2704,2871,2781,2866,2895,2862,2883,2950,2905,2930,2832,2881,2853,2938,2938,2896,2891,2971,3024,2973,2847,2489,2762,2503,2512,2627,2466,2369,2413,3019,3961,3759,2559,2192,2166,2141,2114,2255,2439,2421,2307,2324,2171,2226,2364,2491,2362,2288,2639,2757,2671,2657,2543,2580,2638,2640,2544,2513,2410,2340,2474,2516,2601,2736,2813,2869,2736,2649,2709,2545,2460,2461,2423,2480,2559,2688,2719,2671,2502,2432,2445,2407,2233,2370,2517,2474,2461,2515,2811,3539,4095,3563,2833,2755,2617,2544,2649,2766,2657,2531,2422,2636,2699,2661,2435,2480,2601,2672,2946,3094,3013,2807,2864,2668,2732,2725,2641,2653,2635,2637,2495,2438,2419,2554,2623,2763,2738,2641,2466,2389,2305,2437,2559,2778,2922,3141,2875,2633,2954,3047,3136,3117,3038,2928,2914,2884,2800,3004,3293,3995,4095,4095,4095,3126,3087,3184,3117,3071,2864,2529,2595,2615,2701,2833,2864,2893,2966,3029,2997,2999,2982,2925,2705,2798,2788,2625,2541,2672,2683,2647,2675,2672,2601,2700,2778,2719,2716,2938,3029,2960,2799,2423,2294,2406,2559,2943,3065,3038,2861,2650,2426,2355,2588,2714,2378,2310,2298,2350,2559,3175,4095,3662,2773,2508,2544,2479,2502,2396,2389,2483,2491,2608,2542,2448,2449,2411,2426,2448,2465,2384,2350,2470,2587,2704,2786,2662,2559,2487,2485,2422,2386,2389,1936,1956,2147,2365,2343,2366,2368,2467,2379,2399,2431,2487,2665,2746,2688,2686,2768,2905,2843,2878,2981,2948,2847,3042,3064,3462,4095,4095,1744,2111,2494,2499,2670,3247,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4095,4073,3873,3855,3457,3155,3215,3135,3627,4095,4095,4095,3777,3598,3470,3193,2930,2753,2852,2945,3102,3132,3235,3333,3427,3477,3429,3280,3300,3375,3344,3248,3176,3042,2893,2802,2723,2677,2756,2773,2842,2883,2831,2742,2730,2559,2470,2370,2352,2302,2438,2416,2320,2142,2126,2106,1957,1927,2063,2033,2058,1991,1933,1770,1901,2096,2343,2999,3689,3379,2726,2455,2405,2317,2535,2603,2587,2578,2523,2603,2622,2704,2871,2759,2633,2765,2672,2735,2763,2753,2660,2672,2663,2656,2559,2653,2714,2755,2767,2586,2498,2396,2308,2292,2229,2102,2157,2127,2347,2443,2454,2418,2449,2444,2434,2240,2268,2258,2400,2523,2583,2617,2553,2644,2945,3635,4095,4017,3010,2672,2606,2556,2518,2530,2559,2635,2638,2721,2846,2925,2925,2948,2975,3050,3025,3067,3029,2987,2845,2878,2905,2785,2770,2817,2559,2804,2857,2772,2677,2393,2141,2473,2550,2683,3103,3158,2827,2774,2826,3061,3312,3443,3223,3061,3007,2908,2835,2995,3127,3114,2939,2828,2913,3381,4095,4095,4095,3082,3109,3043,2951,2897,2778,2781,2781,2816,2725,2735,2742,2723,2640,2666,2647,2608,2672,2688,2780,2736,2768,2735,2663,2545,2511,2437,2482,2641,2847,2794,2716,2736,2768,2814,2983,2994,3191,3258,3006,2871,2929,3118,3279,3281,3335,3600,3455,3234,3056,3008,2960,2991,2895,2906,3445,4095,4095,3405,3051,2992,2883,2853,2832,2951,3107,3063,3002,2930,2807,2775,2619,2402,2293,2181,2095,2095,2313,2513,2703,2869,3061,2960,2941,2721,2727,2687,2589,2553,2555,2581,2627,2533,2559,2559,2559,2506,2492,2531,2454,2469,2627,2554,2615,2591,2700,2497,2538,2449,2465,2416,2416,2395,2703,3578,4095,3318,2470,2352,2501,2630,2666,2723,2781,2800,2851,2895,2864,2835,2852,2875,2900,2917,2800,2807,2777,2822,2873,2966,2950,2912,2922,2861,2843,2753,2669,2720,2785,2791,2831,2803,2853,2896,2896,2799,2817,2832,2935,3067,3047,3057,3062,3061,3058,2947,2861,2905,2894,2955,2960,2897,2950,3398,4095,4095,3902,2985,2874,2745,2744,2738,2740,2671,2641,2559,2766,2803,2863,2896,2851,2938,3063,3069,3100,3227,3248,3147,2928,2910,2781,2687,2719,2611,2559,2599,2606,2508,2698,2598,2667,2730,2682,2640,2559,2608,2519,2519,2522,2509,2509,2579,2676,2558,2559,2499,2407,2437,2368,2559,2543,2512,2448,2850,3840,4095,4042,2927,2553,2537,2542,2489,2654,2723,2773,2542,2525,2609,2679,2781,2797,2832,2844,2864,2893,2929,2911,3025,3023,2930,2870,2863,2842,2827,2679,2610,2559,2543,2546,2496,2458,2638,2694,2605,2534,2484,2398,2422,2529,2649,2559,2767,2911,3018,3093,3098,2994,2960,3024,2876,2658,2599,2677,3184,4095,4095,3758,2932,2786,2923,2978,3152,3088,3080,2885,2694,2671,2525,2418,2601,2559,2752,2608,2559,2579,2542,2627,2588,2685,2677,2509,2267,2178,2155,2147,2239,2218,2278,2178,2081,2434,2515,2559,2631,2704,2633,2611,2620,2631,2720,2812,2909,3008,2878,2768,2846,2865,3018,3308,3431,3219,3268,3307,3359,4095,4095,4095,3716,3307,3223,3029,2923,2978,2959,3117,3126,3194,3089,2949,2915,2962,2967,2887,2832,2751,2866,2996,3110,3120,2997,2891,2770,2512,2633,2817,2771,2869,2939,3063,3029,3067,3046,2960,2960,2959,2823,2762,2695,2791,2858,3027,2951,3093,2917,2989,2854,2708,2666,2430,2290,2477,2389,2382,2471,2427,2481,3171,4095,4071,2818,2384,2421,2390,2389,2368,2389,2386,2447,2459,2301,2255,2288,2355,2374,2485,2687,2864,2718,2672,2519,2544,2467,2490,2688,2897,2953,3120,3043,3050,3022,3142,3159,3135,3155,3199,3008,2833,2863,2882,2864,2767,2778,2914,3008,3158,3113,2938,2846,2829,2869,2846,2789,2875,2655,2605,2587,2898,3789,4095,3664,2928,2860,2747,2511,2130,1907,2109,2103,2173,2763,3178,3259,3050,3037,2929,2850,2737,2582,2631,2790,3043,3371,3248,3121,2989,2877,2787,2703,2618,2579,2690,2594,2618,2742,2705,2702,2726,2703,2662,2753,2631,2588,2508,2641,2631,2599,2544,2537,2422,2387,2558,2512,2529,2611,2559,2609,2559,2601,2966,4095,4095,3815,2926,2859,2759,2534,2643,2798,3030,3184,3157,3070,3167,3237,3219,3023,3011,3069,3141,3169,3003,2902,2903,2841,2846,2765,2705,2690,2668,2645,2665,2591,2587,2559,2645,2625,2666,2559,2653,2683,2739,2843,2716,2640,2638,2885,2995,2837,2622,2279,2098,2222,2676,3114,3568,3647,3381,2937,2911,2897,3629,4095,4095,2864,2462,2402,2474,2526,2768,2875,2687,2656,2607,2517,2499,2587,2604,2636,2592,2617,2435,2519,2640,2621,2534,2608,2503,2267,2329,2273,2305,2215,2288,2304,2223,2330,2303,2327,2363,2512,2599,2611,2634,2559,2544,2645,2675,2630,2530,2697,2810,2911,3042,2928,2849,2769,2757,2829,2847,2736,2899,3920,4095,3799,2983,2819,2802,2800,2814,2725,2767,2683,2630,2714,2798,2758,2707,2850,2815,2674,2630,2752,2705,2769,2866,2937,2976,2913,2901,2944,2912,2943));
        ecg_vals.addAll(getMoreECGData1());
        ArrayList<ECG> ecgs = new ArrayList<>();
        for(int i = 0; i < ecg_vals.size(); i++){
            ECG ecg = new ECG();
            ecg.ecg = (short) Math.round(ecg_vals.get(i));
            ecg.timestamp = startTime + i * 10;
            ecgs.add(ecg);
        }
        return ecgs;
    }

    public long insertDefaultValues(){
        startTime = System.currentTimeMillis();
        HeartRateDao hrs = heartRateDao();
        for (HeartRate hr: getDefaultHeartRates()) {
            hrs.insert(hr);
        }
        ECGDao ecgs = ecgDao();
        for (ECG ecg: getDefaultECGValues()) {
            ecgs.insert(ecg);
        }
        return startTime;
    }
}
