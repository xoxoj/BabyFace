#pragma once

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

class ImageProcessor
{
public:
    ImageProcessor();

    cv::Mat crop(cv::Mat &input, const cv::Rect &roi) const;
};
