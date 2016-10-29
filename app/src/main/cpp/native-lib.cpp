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
    Java_polito_artuino_FilterActivity_xdog(JNIEnv *env, jobject, jstring imageSourcePathString, jstring imageOutputPathString, double kappa, double sigma, double tau, double phi) {

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
}

