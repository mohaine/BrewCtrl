import React from 'react'

const Link = ({ className, children, onClick }) => {
  return (
    <span className={className + " clickable_link"} style={{cursor:"pointer"}}
      onClick={e => {
        onClick()
      }}
    >
      {children}
    </span>
  )
}



export default Link
