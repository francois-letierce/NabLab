#include "Utils.h"

/*---------------------------------------------------------------------------*/
/*---------------------------------------------------------------------------*/

namespace nablalib
{

namespace utils
{

int indexOf(const std::vector<int>& array, const int& value)
{
  for (size_t i=0; i<array.size(); ++i)
    if (array[i] == value)
      return i;
  throw std::out_of_range("Value not in array");
}

// Left simulation time
const utils::Timer::duration_type eta(const int& it, const int& max_it, const double& t, const double& max_t,
                                      const double& delta_t, const utils::Timer& timer) noexcept
{
  return std::min(max_it - it, static_cast<int>((max_t - t) / delta_t)) * timer.meanTime();
}

// Simulation progress
const std::string progress_bar(const int& it, const int& max_it, const double& t, const double& max_t,
                               const size_t& width) noexcept
{
  const float progress(std::max(static_cast<float>(it) / static_cast<float>(max_it),
                                static_cast<float>(t) / static_cast<float>(max_t)));

  const size_t pos(width * progress);
  std::string bar(__CYAN_BKG__);
  for (size_t i(0); i < width; ++i)
    (bar.append(i<=pos?__CYAN_BKG__:__BLUE_BKG__)).append(" ");
  bar.append(__RESET__);
  return bar;
}

}  // namespace utils

}  // namespace nablalib