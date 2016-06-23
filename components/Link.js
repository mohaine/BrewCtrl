import React, { PropTypes } from 'react'

const Link = ({ className, children, onClick }) => {
  return (
    <a className={className} href="#"
       onClick={e => {
         e.preventDefault()
         onClick()
       }}
    >
      {children}
    </a>
  )
}



export default Link
