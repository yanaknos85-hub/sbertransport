import React from "react";

const SVGMock = React.forwardRef((props, ref) => <svg ref={ref} {...props}/>)

export const ReactComponent = SVGMock;
export default SVGMock;