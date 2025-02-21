import React from "react";
import { FaSearch } from "react-icons/fa";

export default function SearchBar() {
  return (
    <div className="flex h-8 w-70 flex-row items-center justify-start bg-white">
      <FaSearch id="pretraga-lokacija" className="ml-3" />
      <input
        className="ml-2 pl-2 text-left"
        type="text"
        placeholder="Куцајте за претрагу"
      />
    </div>
  );
}
