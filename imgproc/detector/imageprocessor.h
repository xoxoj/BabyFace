#pragma once

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

class ImageProcessor
{
public:
    ImageProcessor();

    cv::Mat crop(cv::Mat &input, const cv::Rect &roi) const;

    cv::Mat equalizeHistogram(const cv::Mat &input) const;

    bool applyMorphing() const;
    void applyMorphing(bool apply);

private:
    bool morphing = false;
};
