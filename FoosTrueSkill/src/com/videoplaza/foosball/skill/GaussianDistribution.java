package com.videoplaza.foosball.skill;

public class GaussianDistribution {
   // Intentionally, we're not going to derive related things, but set them all at once
   // to get around some NaN issues

   private double mean;

   private double standardDeviation;

   private double variance;
   private double precision;
   private double precisionMean;

   public GaussianDistribution(double mean, double standardDeviation) {
      this.mean = mean;
      this.standardDeviation = standardDeviation;
      variance = square(standardDeviation);
      precision = 1.0 / variance;
      precisionMean = precision * mean;
   }

   private GaussianDistribution() {
   }

   public GaussianDistribution Clone() {
      GaussianDistribution result = new GaussianDistribution();
      result.mean = mean;
      result.standardDeviation = standardDeviation;
      result.variance = variance;
      result.precision = precision;
      result.precisionMean = precisionMean;
      return result;
   }

   public double getMean() {
      return mean;
   }

   public double getNormalizationConstant() {
      // Great derivation of this is at http://www.astro.psu.edu/~mce/A451_2/A451/downloads/notes0.pdf
      return 1.0 / (Math.sqrt(2 * Math.PI) * standardDeviation);
   }

   // Precision and PrecisionMean are used because they make multiplying and dividing simpler
   // (the the accompanying math paper for more details)
   public double getPrecision() {
      return precision;
   }

   public double getPrecisionMean() {
      return precisionMean;
   }

   public double getStandardDeviation() {
      return standardDeviation;
   }

   public double getVariance() {
      return variance;
   }

   @Override
   public String toString() {
      // Debug help
      return String.format("μ=%1$:1.4g, σ=%2$:0.0000g", mean, standardDeviation);
   }

   /// Computes the absolute difference between two Gaussians
   public static double absoluteDifference(GaussianDistribution left, GaussianDistribution right) {
      return Math.max(Math.abs(left.precisionMean - right.precisionMean), Math.sqrt(Math.abs(left.precision - right.precision)));
   }

   public static double at(double x) {
      return at(x, 0, 1);
   }

   public static double at(double x, double mean, double standardDeviation) {
      // See http://mathworld.wolfram.com/NormalDistribution.html
      //                1              -(x-mean)^2 / (2*stdDev^2)
      // P(x) = ------------------- * e
      //        stdDev * sqrt(2*pi)

      double multiplier = 1.0 / (standardDeviation * Math.sqrt(2 * Math.PI));
      double expPart = Math.exp((-1.0 * Math.pow(x - mean, 2.0)) / (2 * (standardDeviation * standardDeviation)));
      double result = multiplier * expPart;
      return result;
   }

   public static double cumulativeTo(double x) {
      return cumulativeTo(x, 0, 1);
   }

   public static double cumulativeTo(double x, double mean, double standardDeviation) {
      double invsqrt2 = -0.707106781186547524400844362104;
      double result = errorFunctionCumulativeTo(invsqrt2 * x);
      return 0.5 * result;
   }

   public static GaussianDistribution divide(GaussianDistribution numerator, GaussianDistribution denominator) {
      return fromPrecisionMean(numerator.precisionMean - denominator.precisionMean, numerator.precision - denominator.precision);
   }

   public static GaussianDistribution fromPrecisionMean(double precisionMean, double precision) {
      GaussianDistribution gaussianDistribution = new GaussianDistribution();
      gaussianDistribution.precision = precision;
      gaussianDistribution.precisionMean = precisionMean;
      gaussianDistribution.variance = 1.0 / precision;
      gaussianDistribution.standardDeviation = Math.sqrt(gaussianDistribution.variance);
      gaussianDistribution.mean = gaussianDistribution.precisionMean / gaussianDistribution.precision;
      return gaussianDistribution;
   }

   public static double inverseCumulativeTo(double x) {
      return inverseCumulativeTo(x, 0, 1);
   }

   public static double inverseCumulativeTo(double x, double mean, double standardDeviation) {
      // From numerical recipes, page 320
      return mean - Math.sqrt(2) * standardDeviation * inverseErrorFunctionCumulativeTo(2 * x);
   }

   public static double logProductNormalization(GaussianDistribution left, GaussianDistribution right) {
      if ((left.precision == 0) || (right.precision == 0)) {
         return 0;
      }

      double varianceSum = left.variance + right.variance;
      double meanDifference = left.mean - right.mean;

      double logSqrt2Pi = Math.log(Math.sqrt(2 * Math.PI));
      return -logSqrt2Pi - (Math.log(varianceSum) / 2.0) - (square(meanDifference) / (2.0 * varianceSum));
   }

   public static double logRatioNormalization(GaussianDistribution numerator, GaussianDistribution denominator) {
      if ((numerator.precision == 0) || (denominator.precision == 0)) {
         return 0;
      }

      double varianceDifference = denominator.variance - numerator.variance;
      double meanDifference = numerator.mean - denominator.mean;

      double logSqrt2Pi = Math.log(Math.sqrt(2 * Math.PI));

      return Math.log(denominator.variance) + logSqrt2Pi - Math.log(varianceDifference) / 2.0 + square(meanDifference) / (2 * varianceDifference);
   }

   // Although we could use equations from // For details, see http://www.tina-vision.net/tina-knoppix/tina-memo/2003-003.pdf
   // for multiplication, the precision mean ones are easier to write :)
   public static GaussianDistribution multiply(GaussianDistribution left, GaussianDistribution right) {
      return fromPrecisionMean(left.precisionMean + right.precisionMean, left.precision + right.precision);
   }

   /// Computes the absolute difference between two Gaussians
   public static double subtract(GaussianDistribution left, GaussianDistribution right) {
      return absoluteDifference(left, right);
   }

   private static double errorFunctionCumulativeTo(double x) {
      // Derived from page 265 of Numerical Recipes 3rd Edition            
      double z = Math.abs(x);

      double t = 2.0 / (2.0 + z);
      double ty = 4 * t - 2;

      double[] coefficients = { -1.3026537197817094, 6.4196979235649026e-1, 1.9476473204185836e-2, -9.561514786808631e-3, -9.46595344482036e-4,
            3.66839497852761e-4, 4.2523324806907e-5, -2.0278578112534e-5, -1.624290004647e-6, 1.303655835580e-6, 1.5626441722e-8, -8.5238095915e-8,
            6.529054439e-9, 5.059343495e-9, -9.91364156e-10, -2.27365122e-10, 9.6467911e-11, 2.394038e-12, -6.886027e-12, 8.94487e-13, 3.13092e-13,
            -1.12708e-13, 3.81e-16, 7.106e-15, -1.523e-15, -9.4e-17, 1.21e-16, -2.8e-17 };

      int ncof = coefficients.length;
      double d = 0.0;
      double dd = 0.0;

      for (int j = ncof - 1; j > 0; j--) {
         double tmp = d;
         d = ty * d - dd + coefficients[j];
         dd = tmp;
      }

      double ans = t * Math.exp(-z * z + 0.5 * (coefficients[0] + ty * d) - dd);
      return x >= 0.0 ? ans : (2.0 - ans);
   }

   private static double inverseErrorFunctionCumulativeTo(double p) {
      // From page 265 of numerical recipes                       

      if (p >= 2.0) {
         return -100;
      }
      if (p <= 0.0) {
         return 100;
      }

      double pp = (p < 1.0) ? p : 2 - p;
      double t = Math.sqrt(-2 * Math.log(pp / 2.0)); // Initial guess
      double x = -0.70711 * ((2.30753 + t * 0.27061) / (1.0 + t * (0.99229 + t * 0.04481)) - t);

      for (int j = 0; j < 2; j++) {
         double err = errorFunctionCumulativeTo(x) - pp;
         x += err / (1.12837916709551257 * Math.exp(-(x * x)) - x * err); // Halley                
      }

      return p < 1.0 ? x : -x;
   }

   private static double square(double x) {
      return x * x;
   }
}
