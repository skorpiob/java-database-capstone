/*
  This script handles the admin dashboard functionality for managing doctors:
  - Loads all doctor cards
  - Filters doctors by name, time, or specialty
  - Adds a new doctor via modal form
*/

import { openModal, closeModal } from '../components/modals.js';
import { getDoctors, filterDoctors, saveDoctor } from './services/doctorServices.js';
import { createDoctorCard } from './components/doctorCard.js';


/*
  Attach a click listener to the "Add Doctor" button
  When clicked, it opens a modal form using openModal('addDoctor')
*/
const addDoctorBtn = document.getElementById("addDoctorBtn");
if (addDoctorBtn) {
  addDoctorBtn.addEventListener("click", () => {
    openModal("addDoctor");
  });
}


/*
  When the DOM is fully loaded:
    - Call loadDoctorCards() to fetch and display all doctors
*/
document.addEventListener("DOMContentLoaded", () => {
  loadDoctorCards();
});


/*
  Function: loadDoctorCards
  Purpose: Fetch all doctors and display them as cards

    Call getDoctors() from the service layer
    Clear the current content area
    For each doctor returned:
    - Create a doctor card using createDoctorCard()
    - Append it to the content div

    Handle any fetch errors by logging them
*/
async function loadDoctorCards() {
  try {
    const doctors = await getDoctors();
    const content = document.getElementById("content");

    if (!content) return;

    content.innerHTML = "";

    doctors.forEach((doctor) => {
      const card = createDoctorCard(doctor);
      content.appendChild(card);
    });

  } catch (error) {
    console.error("Error loading doctor cards:", error);
  }
}


/*
  Attach 'input' and 'change' event listeners to the search bar and filter dropdowns
  On any input change, call filterDoctorsOnChange()
*/
const searchBar = document.getElementById("searchBar");
const timeFilter = document.getElementById("timeFilter");
const specialtyFilter = document.getElementById("specialtyFilter");

if (searchBar) {
  searchBar.addEventListener("input", filterDoctorsOnChange);
}

if (timeFilter) {
  timeFilter.addEventListener("change", filterDoctorsOnChange);
}

if (specialtyFilter) {
  specialtyFilter.addEventListener("change", filterDoctorsOnChange);
}


/*
  Function: filterDoctorsOnChange
  Purpose: Filter doctors based on name, available time, and specialty

    Read values from the search bar and filters
    Normalize empty values to null
    Call filterDoctors(name, time, specialty) from the service

    If doctors are found:
    - Render them using createDoctorCard()
    If no doctors match the filter:
    - Show a message: "No doctors found with the given filters."

    Catch and display any errors with an alert
*/
async function filterDoctorsOnChange() {
  try {
    const name = searchBar?.value.trim() || null;
    const time = timeFilter?.value || null;
    const specialty = specialtyFilter?.value || null;

    const result = await filterDoctors(name, time, specialty);

    if (result.doctors && result.doctors.length > 0) {
      renderDoctorCards(result.doctors);
    } else {
      const content = document.getElementById("content");
      content.innerHTML = `<p>No doctors found with the given filters.</p>`;
    }

  } catch (error) {
    console.error(error);
    alert("Error while filtering doctors.");
  }
}


/*
  Function: renderDoctorCards
  Purpose: A helper function to render a list of doctors passed to it

    Clear the content area
    Loop through the doctors and append each card to the content area
*/
function renderDoctorCards(doctors) {
  const content = document.getElementById("content");
  if (!content) return;

  content.innerHTML = "";

  doctors.forEach((doctor) => {
    const card = createDoctorCard(doctor);
    content.appendChild(card);
  });
}


/*
  Function: adminAddDoctor
  Purpose: Collect form data and add a new doctor to the system

    Collect input values from the modal form
    - Includes name, email, phone, password, specialty, and available times

    Retrieve the authentication token from localStorage
    - If no token is found, show an alert and stop execution

    Build a doctor object with the form values

    Call saveDoctor(doctor, token) from the service

    If save is successful:
    - Show a success message
    - Close the modal and reload the page

    If saving fails, show an error message
*/
window.adminAddDoctor = async function () {
  const name = document.getElementById("doctorName")?.value;
  const email = document.getElementById("doctorEmail")?.value;
  const phone = document.getElementById("doctorPhone")?.value;
  const password = document.getElementById("doctorPassword")?.value;
  const specialty = document.getElementById("doctorSpecialty")?.value;
  const availableTimes = document.getElementById("doctorTimes")?.value;

  const token = localStorage.getItem("token");

  if (!token) {
    alert("Authentication token not found. Please log in again.");
    return;
  }

  const doctor = {
    name,
    email,
    phone,
    password,
    specialty,
    availableTimes
  };

  const result = await saveDoctor(doctor, token);

  if (result.success) {
    alert("Doctor added successfully.");
    closeModal("addDoctor");
    location.reload();
  } else {
    alert(result.message || "Failed to add doctor.");
  }
};