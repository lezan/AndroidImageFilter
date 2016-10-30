#include <jni.h>
#include <string>
#include <opencv2/core.hpp>
#include <opencv2/imgproc.hpp>
#include <opencv2/highgui.hpp>
#include <stdexcept>

using namespace std;
using namespace cv;

extern "C" {

    bool saveResult(const string& filename, const Mat& img) {
        Mat res;
        img.convertTo(res, img.depth());
        if (res.depth() != CV_8U) {
            res.convertTo(res, CV_8U, 255.0);
        }
        bool result = false;
        try {
            result = imwrite(filename, res);
        }
        catch (runtime_error& ex) {
            string cosa = ex.what();
            return result;
        }
        return result;
    }

    void edgeXDoG(InputArray input, OutputArray output, const double kappa, const double sigma,
                  const double tau, const double phi) {
        Mat gray = input.getMat();
        Mat &edge = output.getMatRef();

        const int width = gray.cols;
        const int height = gray.rows;
        //const int dim = gray.channels();

        Mat g1, g2;
        GaussianBlur(gray, g1, Size(0, 0), sigma);
        GaussianBlur(gray, g2, Size(0, 0), kappa * sigma);

        edge = Mat(height, width, CV_32FC1);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                double diff = g1.at<float>(y, x) - tau * g2.at<float>(y, x);
                if (diff > 0.0) {
                    edge.at<float>(y, x) = 1.0f;
                } else {
                    edge.at<float>(y, x) = static_cast<float>(1.0 + tanh(phi * diff));
                }
            }
        }
    }

    void edgeDoG(InputArray image, OutputArray edge, const double kappa, const double sigma,
                 const double tau, const double phi) {
        Mat input = image.getMat();

        Mat &outRef = edge.getMatRef();

        edgeXDoG(input, outRef, kappa, sigma, tau, phi);
    }

    Mat mainDOG(const Mat& img, double kappa, double sigma, double tau, double phi) {
        Mat gray, xdog;
        cvtColor(img, gray, COLOR_BGR2GRAY);

        double defaultKappa = 4.5;
        double defaultSigma = 0.5;
        double defaultTau = 0.95;
        double defaultPhi = 10.0;

        edgeDoG(gray, xdog, kappa, sigma, tau, phi);

        return xdog;
    }

    jstring
    Java_lezan_androidimagefilter_FilterActivity_xdog(JNIEnv *env, jobject, jstring imageSourcePathString, jstring imageOutputPathString, double kappa, double sigma, double tau, double phi) {

        const char* imageSourcePath = env->GetStringUTFChars(imageSourcePathString, 0);
        const char* imageOutputPath = env->GetStringUTFChars(imageOutputPathString, 0);
        string imageNameOutput = "/xdog.png";
        string pathWhereToSave = imageOutputPath + imageNameOutput;

        Mat img = imread(imageSourcePath, IMREAD_COLOR);

        img.convertTo(img, CV_32F, 1.0 / 255.0);
        Mat xdog = mainDOG(img, kappa, sigma, tau, phi);

        bool result = saveResult(pathWhereToSave, xdog);

        if(!result) {
            return env->NewStringUTF("False");
        }
        return env->NewStringUTF(pathWhereToSave.c_str());
    }

    jstring
    Java_lezan_androidimagefilter_FilterActivity_adaptiveThreshold(JNIEnv *env, jobject, jstring imageSourcePathString, jstring imageOutputPathString, int adaptiveMethod, int blockSize, double constant) {

        const char* imageSourcePath = env->GetStringUTFChars(imageSourcePathString, 0);
        const char* imageOutputPath = env->GetStringUTFChars(imageOutputPathString, 0);
        string imageNameOutput = "/threshold.png";
        string pathWhereToSave = imageOutputPath + imageNameOutput;

        Mat img = imread(imageSourcePath, IMREAD_COLOR);

        /* Creo una nuova matrice a partire dalla matrice sorgente */
        Mat gray;
        /* Converto la matrice sorgente in scala di grigi */
        cvtColor(img, gray, COLOR_BGR2GRAY);

        /* Applico una sfocatura all'immagine in scale di grigi */
        medianBlur(gray, gray, 5);
        //GaussianBlur(gray, gray, Size(5, 5), 0);

        /* Creo la matrice che conterrà il risultato finale */
        Mat result;
        /* Applico il threshold alla matrice in scala di grigi ottenendo un'immagine binaria */
        adaptiveThreshold(gray, result, 255, adaptiveMethod, THRESH_BINARY, blockSize, constant);

        bool final = imwrite(pathWhereToSave, result);

        if(!final) {
            return env->NewStringUTF("False");
        }
        return env->NewStringUTF(pathWhereToSave.c_str());
    }

    jstring
    Java_lezan_androidimagefilter_FilterActivity_xdog2(JNIEnv *env, jobject, jstring imageSourcePathString, jstring imageOutputPathString) {
        const char* imageSourcePath = env->GetStringUTFChars(imageSourcePathString, 0);
        const char* imageOutputPath = env->GetStringUTFChars(imageOutputPathString, 0);
        string imageNameOutput = "/xdog2.png";
        string pathWhereToSave = imageOutputPath + imageNameOutput;

        Mat img = imread(imageSourcePath, IMREAD_COLOR);

        /* Creo una nuova matrice a partire dalla matrice sorgente */
        Mat gray;
        /* Converto la matrice sorgente in scala di grigi */
        cvtColor(img, gray, COLOR_BGR2GRAY);

        /* Creo la matrice che conterrà il risultato finale */
        Mat result;

        double tao = 0.9;
        double eps = 3;
        double phi = 20/255.0;

        Mat filter1;
        GaussianBlur(gray, filter1, Size(5, 5), 6);
        Mat filter2;
        GaussianBlur(gray, filter2, Size(5, 5), 1.5);

        Mat image1;
        filter2D(filter1, image1, -1, BORDER_CONSTANT);
        Mat image2;
        filter2D(filter2, image2, -1, BORDER_CONSTANT);

        result = image2 - tao*image1;

        Mat output = Mat::zeros(result.size(), result.type());

        int min = 1;

        for(int i = 1; i < result.rows; i++) {
            for(int j = 1; j < result.cols; j++) {
                if(result.at<float>(i, j) >= eps) {
                    output.at<float>(i, j) = 1;
                }
                else {
                    output.at<float>(i, j) = 1 + tanh(phi*(result.at<float>(i, j)) - eps);
                 }
            }
        }

        bool final = imwrite(pathWhereToSave, output);

        if(!final) {
            return env->NewStringUTF("False");
        }
        return env->NewStringUTF(pathWhereToSave.c_str());
    }
}

