import React from "react";

export default function Header() {
  return (
    <div className="flex h-25 w-screen flex-row justify-between bg-blue-600">
      <div className="ml-26 flex w-2/5 flex-row items-center justify-start">
        <button className="mr-6 bg-amber-300 hover:bg-blue-600">
          КУПОВИНА
        </button>
        <button className="mr-6">ИЗНАЈМЉИВАЊЕ</button>
        <button>ПРОДАЈА</button>
      </div>
      <div className="logo w-2/5"></div>
      <div className="mr-26 flex w-1/5 flex-row items-center justify-end">
        <button className="mr-6">ПОМОЋ</button>
        <button>ПРИЈАВА</button>
      </div>
    </div>
  );
}
