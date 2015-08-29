#pragma once

#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/highgui/highgui.hpp>

class ImageProcessor
{
public:
    ImageProcessor();

    cv::Mat crop(const cv::Mat &input, const cv::Rect &roi) const;

    cv::Mat scale(const cv::Mat &input) const;

    cv::Mat equalizeHistogram(const cv::Mat &input) const;

    bool applyMorphing() const;
    void applyMorphing(const bool apply);

    bool enhance() const;
    void enhance(const bool apply);

private:
    bool morphing = false;
    bool enhancement = false;
};
