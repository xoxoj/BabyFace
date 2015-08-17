#pragma once

#include <opencv2/objdetect/objdetect.hpp>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>

class FaceDetector
{
public:
    FaceDetector();

    cv::Rect detect(const cv::Mat &input);

private:
    cv::Rect findLargestFace(const std::vector<cv::Rect> &faces) const;

/**
  * DATA
  */
private:
    cv::CascadeClassifier classifier;
};
