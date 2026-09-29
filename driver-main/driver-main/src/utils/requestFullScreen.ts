// eslint-disable-next-line @typescript-eslint/no-explicit-any
export function requestFullScreen(element: any) {
  // Supports most browsers and their versions.
  const requestMethod
    = element.requestFullScreen
    || element.webkitRequestFullScreen
    || element.mozRequestFullScreen
    || element.msRequestFullScreen;

  if (requestMethod) {
    // Native full screen.
    requestMethod.call(element);
  }
}
